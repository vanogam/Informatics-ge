package ge.freeuni.informatics.acceptance;

import ge.freeuni.informatics.acceptance.base.BaseAcceptanceTest;
import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.SubmissionKind;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.controller.model.StandingsResponse;
import ge.freeuni.informatics.controller.model.SubmissionListResponse;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.annotation.DirtiesContext;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * The IOI "BatchAndOutput" flow end to end: a contest whose tasks accumulate per-subtask maxima
 * across submissions, and a task that accepts the contestant's own output files instead of code.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class OutputSubmissionAcceptanceTest extends BaseAcceptanceTest {

    private static final String PASSWORD = "password123";
    /** Ten tests keyed "1".."10", each worth 10 points under SUM. */
    private static final int NUM_TESTCASES = 10;

    private ContestRoom room;
    private Contest contest;
    private Task task;
    private User student;

    @BeforeEach
    void setUp() {
        room = testDataFactory.createEmptyRoom("Output Room");
        User teacher = testDataFactory.createTeacher("teacher", PASSWORD);
        testDataFactory.setRoomTeacher(room, teacher);
        List<User> students = testDataFactory.createStudents(1, "student", PASSWORD);
        student = students.get(0);
        testDataFactory.addParticipantsToRoom(room, students);
        contest = testDataFactory.createLiveSubtaskMaxContest("Output Contest", room, 60);
        task = testDataFactory.createTask(contest, "A", "Magic City", NUM_TESTCASES);
        testDataFactory.registerUsersForContest(contest, students);
    }

    @AfterEach
    void tearDown() {
        clearAllSessions();
        mockKafkaWorker.clearScores();
    }

    @Test
    @DisplayName("Two partial submissions accumulate into the union of the subtasks they solved")
    void testSubtaskMaxAccumulatesAcrossSubmissions() {
        Long first = submitCode();
        mockKafkaWorker.setPassingTestKeys(first, Set.of("1", "2", "3"));
        waitForSubmissionsToComplete();

        Long second = submitCode();
        mockKafkaWorker.setPassingTestKeys(second, Set.of("3", "4"));
        waitForSubmissionsToComplete();

        // The union of the two: tests 1-4. Test 3, solved by both, is counted once - which is
        // exactly what BEST_SUBMISSION could not do, since neither submission scored above 30.
        assertThat(standingsScore()).isEqualTo(40.0f);
    }

    @Test
    @DisplayName("A submission that solves less than the accumulated best does not lower it")
    void testSubtaskMaxNeverGoesDown() {
        Long first = submitCode();
        mockKafkaWorker.setPassingTestKeys(first, Set.of("1", "2", "3", "4", "5"));
        waitForSubmissionsToComplete();

        Long second = submitCode();
        mockKafkaWorker.setPassingTestKeys(second, Set.of("1"));
        waitForSubmissionsToComplete();

        assertThat(standingsScore()).isEqualTo(50.0f);
    }

    @Test
    @DisplayName("A zip of output files is judged as the tests it covers, the rest scoring zero")
    void testOutputZipIsJudgedForTheTestsItCovers() throws IOException {
        testDataFactory.setSubmissionKinds(task, true, true);

        Response response = uploadOutputs("outputs.zip",
                zipOf(Map.of("test1.out", "1", "test2.out", "2", "test3.out", "3")), null);

        response.then().statusCode(200);
        assertThat(response.jsonPath().getInt("matchedTests")).isEqualTo(3);
        assertThat(response.jsonPath().getInt("totalTests")).isEqualTo(NUM_TESTCASES);

        waitForSubmissionsToComplete();

        SubmissionDTO submission = latestSubmission();
        assertThat(submission.kind()).isEqualTo(SubmissionKind.OUTPUT);
        // Only the three uploaded tests were judged; the other seven were recorded as zeros
        // without ever reaching a worker.
        assertThat(submission.score()).isEqualTo(30.0f);
        assertThat(submission.status()).isEqualTo(SubmissionStatus.PARTIAL);
    }

    @Test
    @DisplayName("A single output file is judged against the test it names")
    void testSingleOutputFileIsJudgedForItsTest() {
        testDataFactory.setSubmissionKinds(task, true, true);

        uploadOutputs("test4.out", "4".getBytes(StandardCharsets.UTF_8), null)
                .then()
                .statusCode(200)
                .body("matchedTests", org.hamcrest.Matchers.equalTo(1));

        waitForSubmissionsToComplete();
        assertThat(latestSubmission().score()).isEqualTo(10.0f);
    }

    @Test
    @DisplayName("An upload matching no test is refused rather than queued")
    void testUploadWithNoMatchingTestIsRefused() {
        testDataFactory.setSubmissionKinds(task, true, true);

        uploadOutputs("nonsense.txt", "hello".getBytes(StandardCharsets.UTF_8), null)
                .then()
                .body("message", org.hamcrest.Matchers.equalTo("noMatchingOutputs"));
    }

    @Test
    @DisplayName("A task that does not accept outputs refuses the upload")
    void testOutputUploadRefusedWhenTaskDoesNotAllowIt() {
        uploadOutputs("test1.out", "1".getBytes(StandardCharsets.UTF_8), null)
                .then()
                .body("message", org.hamcrest.Matchers.equalTo("outputSubmissionNotAllowed"));
    }

    @Test
    @DisplayName("A task that only takes outputs refuses a code submission")
    void testCodeSubmissionRefusedForOutputOnlyTask() {
        testDataFactory.setSubmissionKinds(task, false, true);

        Map<String, Object> request = new HashMap<>();
        request.put("contestId", contest.getId().intValue());
        request.put("taskId", task.getId().intValue());
        request.put("submissionText", "int main() { return 0; }");
        request.put("language", CodeLanguage.CPP.name());

        givenUser(student.getUsername(), PASSWORD)
                .body(request)
                .when()
                .post("/submit")
                .then()
                .body("message", org.hamcrest.Matchers.equalTo("codeSubmissionNotAllowed"));
    }

    /**
     * An output upload accumulates like any other submission, which is the whole point of pairing
     * the two features: a contestant answers one test at a time and keeps every test they got.
     */
    @Test
    @DisplayName("Output submissions accumulate under SUBTASK_MAX")
    void testOutputSubmissionsAccumulate() throws IOException {
        testDataFactory.setSubmissionKinds(task, true, true);

        uploadOutputs("outputs.zip", zipOf(Map.of("test1.out", "1", "test2.out", "2")), null)
                .then()
                .statusCode(200);
        waitForSubmissionsToComplete();

        uploadOutputs("test5.out", "5".getBytes(StandardCharsets.UTF_8), null)
                .then()
                .statusCode(200);
        waitForSubmissionsToComplete();

        assertThat(standingsScore()).isEqualTo(30.0f);
    }

    // ---- helpers ---------------------------------------------------------------------------

    private Long submitCode() {
        Map<String, Object> request = new HashMap<>();
        request.put("contestId", contest.getId().intValue());
        request.put("taskId", task.getId().intValue());
        request.put("submissionText", "int main() { return 0; }");
        request.put("language", CodeLanguage.CPP.name());
        return givenUser(student.getUsername(), PASSWORD)
                .body(request)
                .when()
                .post("/submit")
                .then()
                .statusCode(200)
                .extract()
                .jsonPath()
                .getLong("submissionId");
    }

    private Response uploadOutputs(String fileName, byte[] content, String testKey) {
        var request = givenUser(student.getUsername(), PASSWORD)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", fileName, content)
                .multiPart("taskId", String.valueOf(task.getId()));
        if (testKey != null) {
            request = request.multiPart("testKey", testKey);
        }
        return request.when().post("/submit/output");
    }

    /**
     * The mock worker answers a test correctly when its key is in the passing set; an uploaded
     * output is judged the same way, so a test the upload covers is marked as passing by key.
     */
    private byte[] zipOf(Map<String, String> filesByName) throws IOException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            for (Map.Entry<String, String> file : filesByName.entrySet()) {
                zip.putNextEntry(new ZipEntry(file.getKey()));
                zip.write(file.getValue().getBytes(StandardCharsets.UTF_8));
                zip.closeEntry();
            }
        }
        return bytes.toByteArray();
    }

    private SubmissionDTO latestSubmission() {
        SubmissionListResponse response = givenUser(student.getUsername(), PASSWORD)
                .queryParam("taskId", task.getId())
                .when()
                .get("/contest/{contestId}/submissions", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(SubmissionListResponse.class);
        assertThat(response.getSubmissions()).isNotEmpty();
        return response.getSubmissions().get(0);
    }

    private float standingsScore() {
        StandingsResponse standings = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}/standings", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(StandingsResponse.class);
        ContestantResultDTO result = standings.getStandings()
                .stream()
                .filter(r -> r.contestantId().equals(student.getId()))
                .findFirst()
                .orElseThrow();
        return result.totalScore();
    }

    private void waitForSubmissionsToComplete() {
        await()
                .atMost(15, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    Thread.sleep(200);
                    SubmissionListResponse response = givenUser("teacher", PASSWORD)
                            .when()
                            .get("/contest/{contestId}/status", contest.getId())
                            .then()
                            .extract()
                            .as(SubmissionListResponse.class);
                    if (response.getSubmissions() == null || response.getSubmissions().isEmpty()) {
                        return false;
                    }
                    return response.getSubmissions().stream()
                            .allMatch(s -> s.status() != SubmissionStatus.IN_QUEUE
                                    && s.status() != SubmissionStatus.COMPILING
                                    && s.status() != SubmissionStatus.RUNNING);
                });
    }
}
