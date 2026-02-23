package ge.freeuni.informatics.acceptance;

import ge.freeuni.informatics.acceptance.base.BaseAcceptanceTest;
import ge.freeuni.informatics.common.dto.ContestDTO;
import ge.freeuni.informatics.common.dto.ContestantResultDTO;
import ge.freeuni.informatics.common.dto.SubmissionDTO;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.submission.SubmissionStatus;
import ge.freeuni.informatics.common.model.submission.TestStatus;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import ge.freeuni.informatics.controller.model.RegistrantsResponse;
import ge.freeuni.informatics.controller.model.StandingsResponse;
import ge.freeuni.informatics.controller.model.SubmissionListResponse;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.test.annotation.DirtiesContext;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

/**
 * Full acceptance test for the contest flow.
 * 
 * This test simulates a complete contest scenario with 10 users:
 * - Contest creation and setup
 * - User registration
 * - Submissions with various results
 * - Standings verification
 * - Upsolving functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class ContestAcceptanceTest extends BaseAcceptanceTest {

    private static final String PASSWORD = "password123";
    private static final int NUM_USERS = 10;
    private static final int NUM_TESTCASES = 10; // Mock task: input/output n where 1 <= n <= 10

    private User teacher;
    private List<User> students;
    private ContestRoom room;
    private Contest contest;
    private Task taskA;
    private Task taskB;

    @BeforeEach
    void setUp() {
        // Fresh database and Spring context for each test due to @DirtiesContext
        room = testDataFactory.createEmptyRoom("Test Room");
        teacher = testDataFactory.createTeacher("teacher", PASSWORD);
        testDataFactory.setRoomTeacher(room, teacher);
        students = testDataFactory.createStudents(NUM_USERS, "student", PASSWORD);
        testDataFactory.addParticipantsToRoom(room, students);
        contest = testDataFactory.createLiveContest("Test Contest", room, 60);
        taskA = testDataFactory.createTask(contest, "A", "Task A - Easy", NUM_TESTCASES);
        taskB = testDataFactory.createTask(contest, "B", "Task B - Hard", NUM_TESTCASES);
        testDataFactory.registerUsersForContest(contest, students);
    }

    @AfterEach
    void tearDown() {
        clearAllSessions();
        mockKafkaWorker.clearScores();
    }

    @Test
    @Order(1)
    @DisplayName("Verify registrants are correctly listed")
    void testRegistrantsAreCorrectlyListed() {
        RegistrantsResponse response = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}/registrants", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(RegistrantsResponse.class);

        assertThat(response.getRegistrants())
                .hasSize(NUM_USERS)
                .extracting("username")
                .containsExactlyInAnyOrderElementsOf(
                        students.stream().map(User::getUsername).toList()
                );
    }

    @Test
    @Order(2)
    @DisplayName("Verify user can check registration status")
    void testUserCanCheckRegistrationStatus() {
        Response response = givenUser("student1", PASSWORD)
                .when()
                .get("/contest/{contestId}/is-registered", contest.getId());

        assertThat(response.statusCode()).isEqualTo(200);
        assertThat(response.jsonPath().getBoolean("registered")).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("Submit correct solution and verify score")
    void testCorrectSubmissionScoring() {
        submitAndVerify("student1", taskA.getId(), SubmissionStatus.CORRECT, 100.0f);
    }

    @Test
    @Order(4)
    @DisplayName("Submit partial solution and verify score")
    void testPartialSubmissionScoring() {
        // 60% score = 6 out of 10 tests pass
        submitAndVerify("student2", taskB.getId(), SubmissionStatus.PARTIAL, 60.0f);
    }

    @Test
    @Order(5)
    @DisplayName("Submit wrong answer and verify score")
    void testWrongAnswerSubmission() {
        submitAndVerify("student3", taskA.getId(), SubmissionStatus.FAILED, 0.0f);
    }

    @Test
    @Order(6)
    @DisplayName("Verify standings after multiple submissions")
    void testStandingsAfterMultipleSubmissions() {
        Map<String, Float> expectedScores = new HashMap<>();

        // Student 1: 100 on A, 0 on B = 100
        submitWithScore("student1", taskA.getId(), 100);
        submitWithScore("student1", taskB.getId(), 0);
        expectedScores.put("student1", 100.0f);

        // Student 2: 100 on A, 60 on B = 160
        submitWithScore("student2", taskA.getId(), 100);
        submitWithScore("student2", taskB.getId(), 60);
        expectedScores.put("student2", 160.0f);

        // Student 3: 0 on A, 100 on B = 100
        submitWithScore("student3", taskA.getId(), 0);
        submitWithScore("student3", taskB.getId(), 100);
        expectedScores.put("student3", 100.0f);

        // Student 4: 100 on both = 200
        submitWithScore("student4", taskA.getId(), 100);
        submitWithScore("student4", taskB.getId(), 100);
        expectedScores.put("student4", 200.0f);

        waitForSubmissionsToComplete();

        StandingsResponse standings = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}/standings", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(StandingsResponse.class);

        assertThat(standings.getStandings()).isNotNull();
        
        // Verify standings are ordered by total score (descending)
        List<ContestantResultDTO> orderedStandings = new ArrayList<>(standings.getStandings());
        orderedStandings.sort(Comparator.comparing(ContestantResultDTO::totalScore).reversed());
        
        // Student 4 should be first (200 points)
        assertThat(orderedStandings.get(0).totalScore()).isEqualTo(200.0f);
        
        // Verify scores for each participating user
        for (ContestantResultDTO result : standings.getStandings()) {
            User user = students.stream()
                    .filter(s -> s.getId().equals(result.contestantId()))
                    .findFirst()
                    .orElse(null);
            
            if (user != null && expectedScores.containsKey(user.getUsername())) {
                assertThat(result.totalScore())
                        .as("Score for %s", user.getUsername())
                        .isEqualTo(expectedScores.get(user.getUsername()));
            }
        }
    }

    @Test
    @Order(7)
    @DisplayName("Verify best submission scoring type")
    void testBestSubmissionScoringType() {
        // First submission: 40% (4 out of 10 tests pass)
        submitWithScore("student5", taskA.getId(), 40);
        waitForSubmissionsToComplete();

        // Second submission: 100% (all tests pass)
        submitWithScore("student5", taskA.getId(), 100);
        waitForSubmissionsToComplete();

        StandingsResponse standings = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}/standings", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(StandingsResponse.class);

        User student5 = students.stream()
                .filter(s -> s.getUsername().equals("student5"))
                .findFirst()
                .orElseThrow();

        ContestantResultDTO student5Result = standings.getStandings().stream()
                .filter(r -> r.contestantId().equals(student5.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(student5Result.totalScore()).isEqualTo(100.0f);
    }

    @Test
    @Order(8)
    @DisplayName("Verify user can view their own submissions")
    void testUserCanViewOwnSubmissions() {
        submitWithScore("student6", taskA.getId(), 100);
        submitWithScore("student6", taskA.getId(), 100);
        
        waitForSubmissionsToComplete();

        User student6 = students.stream()
                .filter(s -> s.getUsername().equals("student6"))
                .findFirst()
                .orElseThrow();

        SubmissionListResponse response = givenUser("student6", PASSWORD)
                .when()
                .get("/contest/{contestId}/submissions", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(SubmissionListResponse.class);

        assertThat(response.getSubmissions())
                .isNotEmpty()
                .allMatch(s -> s.username().equals("student6"));
    }

    @Test
    @Order(9)
    @DisplayName("Verify contest status shows all submissions")
    void testContestStatusShowsAllSubmissions() {
        submitWithScore("student7", taskA.getId(), 100);
        submitWithScore("student8", taskA.getId(), 100);
        submitWithScore("student9", taskA.getId(), 100);

        waitForSubmissionsToComplete();

        SubmissionListResponse response = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}/status", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(SubmissionListResponse.class);

        assertThat(response.getSubmissions()).isNotEmpty();
        
        // Verify we can see submissions from multiple users
        List<String> submittingUsers = response.getSubmissions().stream()
                .map(SubmissionDTO::username)
                .distinct()
                .toList();
        
        assertThat(submittingUsers).hasSizeGreaterThan(1);
    }

     @Test
     @Order(10)
     @DisplayName("Verify compilation error handling")
     void testCompilationErrorHandling() {
         Long submissionId = submitSolution("student10", taskA.getId());
         mockKafkaWorker.setCompilationError(submissionId);

         waitForSubmissionsToComplete();

         SubmissionListResponse response = givenUser("student10", PASSWORD)
                 .when()
                 .get("/contest/{contestId}/submissions", contest.getId())
                 .then()
                 .statusCode(200)
                 .extract()
                 .as(SubmissionListResponse.class);

         assertThat(response.getSubmissions())
                 .isNotEmpty()
                 .anyMatch(s -> s.status() == SubmissionStatus.COMPILATION_ERROR);
     }

    @Test
    @Order(12)
    @DisplayName("Verify user unregistration works for future contest")
    void testUserUnregistration() {
        // Use a future contest — unregistration is only allowed before contest starts
        Contest futureContest = testDataFactory.createFutureContest("Future Contest", room, 60, 60);
        User newStudent = testDataFactory.createStudent("newstudent", PASSWORD);
        testDataFactory.addParticipantsToRoom(room, List.of(newStudent));
        testDataFactory.registerUserForContest(futureContest, newStudent);

        Response response = givenUser("newstudent", PASSWORD)
                .when()
                .get("/contest/{contestId}/is-registered", futureContest.getId());
        assertThat(response.jsonPath().getBoolean("registered")).isTrue();

        givenUser("newstudent", PASSWORD)
                .when()
                .post("/contest/{contestId}/unregister", futureContest.getId())
                .then()
                .statusCode(200);

        clearSession("newstudent");
        response = givenUser("newstudent", PASSWORD)
                .when()
                .get("/contest/{contestId}/is-registered", futureContest.getId());
        assertThat(response.jsonPath().getBoolean("registered")).isFalse();
    }

    @Test
    @Order(13)
    @DisplayName("Verify upsolving works for past contests")
    void testUpsolvingForPastContest() {
        Contest pastContest = testDataFactory.createPastContest("Past Contest", room, true);
        Task upsolvingTask = testDataFactory.createTask(pastContest, "U", "Upsolving Task", NUM_TESTCASES);
        
        testDataFactory.registerUserForContest(pastContest, students.getFirst());

        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("contestId", pastContest.getId().intValue());
        submitRequest.put("taskId", upsolvingTask.getId().intValue());
        submitRequest.put("submissionText", "int main() { return 0; }");
        submitRequest.put("language", CodeLanguage.CPP.name());

        Long submissionId = givenUser("student1", PASSWORD)
                .body(submitRequest)
                .when()
                .post("/submit")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getLong("submissionId");
        
        mockKafkaWorker.setSubmissionScore(submissionId, 100);

        waitForSubmissionsToComplete(pastContest);

        SubmissionListResponse response = givenUser("student1", PASSWORD)
                .when()
                .get("/contest/{contestId}/submissions", pastContest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(SubmissionListResponse.class);

        assertThat(response.getSubmissions())
                .isNotEmpty()
                .anyMatch(s -> s.status() == SubmissionStatus.CORRECT && s.score() == 100.0f);
    }

    @Test
    @Order(14)
    @DisplayName("Verify contest with upsolving disabled rejects submissions after contest ends")
    void testNoUpsolvingWhenDisabled() {
        Contest noUpsolvingContest = testDataFactory.createPastContest("No Upsolving Contest", room, false);
        Task task = testDataFactory.createTask(noUpsolvingContest, "N", "No Upsolving Task", NUM_TESTCASES);

        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("contestId", noUpsolvingContest.getId().intValue());
        submitRequest.put("taskId", task.getId().intValue());
        submitRequest.put("submissionText", "int main() { return 0; }");
        submitRequest.put("language", CodeLanguage.CPP.name());

        givenUser("student1", PASSWORD)
                .body(submitRequest)
                .when()
                .post("/submit")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(15)
    @DisplayName("Verify non-registered user cannot submit during live contest")
    void testNonRegisteredUserCannotSubmitDuringLiveContest() {
        User unregisteredStudent = testDataFactory.createStudent("unregistered", PASSWORD);
        testDataFactory.addParticipantsToRoom(room, List.of(unregisteredStudent));

        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("contestId", contest.getId().intValue());
        submitRequest.put("taskId", taskA.getId().intValue());
        submitRequest.put("submissionText", "int main() { return 0; }");
        submitRequest.put("language", CodeLanguage.CPP.name());

        givenUser("unregistered", PASSWORD)
                .body(submitRequest)
                .when()
                .post("/submit")
                .then()
                .statusCode(400);
    }

    @Test
    @Order(16)
    @DisplayName("Verify live and upsolving standings remain consistent and separate")
    void testUpsolvingStandingsIntegrity() {
        Contest pastContest = testDataFactory.createPastContest("Integrity Contest", room, true);
        Task taskLiveA = testDataFactory.createTask(pastContest, "IA", "Task IA", NUM_TESTCASES);
        Task taskLiveB = testDataFactory.createTask(pastContest, "IB", "Task IB", NUM_TESTCASES);
        User s1 = students.get(0);
        User s2 = students.get(1);
        testDataFactory.registerUserForContest(pastContest, s1);
        testDataFactory.registerUserForContest(pastContest, s2);

        // Simulate frozen live standings: s1 has 100 on IA, 0 on IB; s2 has 0 on IA, 50 on IB
        testDataFactory.setLiveTaskScore(pastContest, s1, "IA", 100f);
        testDataFactory.setLiveTaskScore(pastContest, s2, "IB", 50f);

        // Upsolving submissions: s1 solves IB (100), s2 solves IA (100)
        submitWithScoreForContest("student1", taskLiveB.getId(), 100, pastContest);
        submitWithScoreForContest("student2", taskLiveA.getId(), 100, pastContest);
        waitForSubmissionsToComplete(pastContest);

        // Live standings (frozen) must be unchanged: s1=100, s2=50
        StandingsResponse standingsResponse = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}/standings", pastContest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(StandingsResponse.class);
        List<ContestantResultDTO> liveStandings = standingsResponse.getStandings();
        assertThat(liveStandings).hasSize(2);
        ContestantResultDTO liveS1 = findByContestantId(liveStandings, s1.getId());
        ContestantResultDTO liveS2 = findByContestantId(liveStandings, s2.getId());
        assertThat(liveS1).isNotNull();
        assertThat(liveS2).isNotNull();
        assertThat(liveS1.totalScore()).isEqualTo(100.0f);
        assertThat(liveS2.totalScore()).isEqualTo(50.0f);

        // Upsolving standings are separate and contain only post-contest (upsolving) scores: s1=100 (B), s2=100 (A)
        ContestDTO contestDto = givenUser("teacher", PASSWORD)
                .when()
                .get("/contest/{contestId}", pastContest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(ContestDTO.class);
        List<ContestantResultDTO> upsolvingStandings = contestDto.getUpsolvingStandings();
        assertThat(upsolvingStandings).isNotNull().hasSize(2);
        ContestantResultDTO upS1 = findByContestantId(upsolvingStandings, s1.getId());
        ContestantResultDTO upS2 = findByContestantId(upsolvingStandings, s2.getId());
        assertThat(upS1).isNotNull();
        assertThat(upS2).isNotNull();
        assertThat(upS1.totalScore()).isEqualTo(100.0f);
        assertThat(upS2.totalScore()).isEqualTo(100.0f);
    }

    private static ContestantResultDTO findByContestantId(List<ContestantResultDTO> list, Long contestantId) {
        return list.stream()
                .filter(r -> contestantId.equals(r.contestantId()))
                .findFirst()
                .orElse(null);
    }

    /**
     * Submit a solution and return the submission ID.
     * Use mockKafkaWorker.setSubmissionScore() after this to control the result.
     */
    private Long submitSolution(String username, Long taskId) {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("contestId", contest.getId().intValue());
        submitRequest.put("taskId", taskId.intValue());
        submitRequest.put("submissionText", "int main() { return 0; }");
        submitRequest.put("language", CodeLanguage.CPP.name());

        return givenUser(username, PASSWORD)
                .body(submitRequest)
                .when()
                .post("/submit")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getLong("submissionId");
    }
    
    /**
     * Submit a solution with a specific score percentage (0-100).
     */
    private void submitWithScore(String username, Long taskId, int scorePercentage) {
        Long submissionId = submitSolution(username, taskId);
        mockKafkaWorker.setSubmissionScore(submissionId, scorePercentage);
    }

    /**
     * Submit a solution for a specific contest with a given score percentage (0-100).
     */
    private void submitWithScoreForContest(String username, Long taskId, int scorePercentage, Contest contestForSubmit) {
        Map<String, Object> submitRequest = new HashMap<>();
        submitRequest.put("contestId", contestForSubmit.getId().intValue());
        submitRequest.put("taskId", taskId.intValue());
        submitRequest.put("submissionText", "int main() { return 0; }");
        submitRequest.put("language", CodeLanguage.CPP.name());
        Long submissionId = givenUser(username, PASSWORD)
                .body(submitRequest)
                .when()
                .post("/submit")
                .then()
                .statusCode(200)
                .extract()
                .body()
                .jsonPath()
                .getLong("submissionId");
        mockKafkaWorker.setSubmissionScore(submissionId, scorePercentage);
    }

    private void submitAndVerify(String username, Long taskId, SubmissionStatus expectedStatus, float expectedScore) {
        // Calculate score percentage from expected score (assuming each task is worth 100 points)
        int scorePercentage = (int) expectedScore;
        
        Long submissionId = submitSolution(username, taskId);
        mockKafkaWorker.setSubmissionScore(submissionId, scorePercentage);
        
        waitForSubmissionsToComplete();

        SubmissionListResponse response = givenUser(username, PASSWORD)
                .queryParam("taskId", taskId)
                .when()
                .get("/contest/{contestId}/submissions", contest.getId())
                .then()
                .statusCode(200)
                .extract()
                .as(SubmissionListResponse.class);

        assertThat(response.getSubmissions()).isNotEmpty();
        
        SubmissionDTO latestSubmission = response.getSubmissions().get(0);
        assertThat(latestSubmission.status()).isEqualTo(expectedStatus);
        assertThat(latestSubmission.score()).isEqualTo(expectedScore);
    }

    /**
     * Waits for all submissions to be processed by the mock judge.
     * Polls the submission status until all submissions have a final status (not IN_QUEUE or COMPILING or RUNNING).
     */
    private void waitForSubmissionsToComplete() {
        waitForSubmissionsToComplete(contest);
    }

    private void waitForSubmissionsToComplete(Contest contestToCheck) {
        await()
                .atMost(10, TimeUnit.SECONDS)
                .pollInterval(100, TimeUnit.MILLISECONDS)
                .until(() -> {
                    try {
                        // Give async processing some time to start
                        Thread.sleep(200);
                        
                        // Check if all submissions are in a final state
                        SubmissionListResponse response = givenUser("teacher", PASSWORD)
                                .when()
                                .get("/contest/{contestId}/status", contestToCheck.getId())
                                .then()
                                .extract()
                                .as(SubmissionListResponse.class);
                        
                        if (response.getSubmissions() == null || response.getSubmissions().isEmpty()) {
                            return false; // No submissions yet
                        }
                        
                        // Check if all submissions have completed processing
                        boolean allComplete = response.getSubmissions().stream()
                                .allMatch(s -> s.status() != SubmissionStatus.IN_QUEUE 
                                           && s.status() != SubmissionStatus.COMPILING 
                                           && s.status() != SubmissionStatus.RUNNING);
                        
                        if (!allComplete) {
                            System.out.println("Still waiting for submissions to complete...");
                            System.out.println("STATUS: " + response.getSubmissions().getFirst().status());
                        }
                        
                        return allComplete;
                    } catch (Exception e) {
                        System.err.println("Error while waiting for submissions: " + e.getMessage());
                        return false;
                    }
                });
    }
}
