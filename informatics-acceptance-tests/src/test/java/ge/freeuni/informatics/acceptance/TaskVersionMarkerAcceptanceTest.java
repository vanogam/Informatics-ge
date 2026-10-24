package ge.freeuni.informatics.acceptance;

import ge.freeuni.informatics.acceptance.base.BaseAcceptanceTest;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.annotation.DirtiesContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * The "lastUpdate" marker a worker reads to decide whether its cached copy of a task is stale.
 * Anything that adds, replaces or removes a grader/manager/checker file, or a testcase, must
 * bump it - otherwise a worker keeps judging against the previous copy of the task.
 *
 * <p>Markers are compared with "greater than the value seen before the action", not "the file
 * didn't exist before". A previous run of this same suite can leave a marker file behind at the
 * same path (H2 resets task ids to 1 on each fresh context, but /tmp is not wiped between runs),
 * so "didn't exist yet" would be flaky; "the timestamp moved forward" is not.
 */
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
public class TaskVersionMarkerAcceptanceTest extends BaseAcceptanceTest {

    private static final String PASSWORD = "password123";

    @Value("${ge.freeuni.informatics.Task.taskDirectoryAddress}")
    private String taskDirectoryAddress;

    private User teacher;
    private ContestRoom room;
    private Contest contest;
    private Task task;

    @BeforeEach
    void setUp() {
        room = testDataFactory.createEmptyRoom("Marker Test Room");
        teacher = testDataFactory.createTeacher("marker-teacher", PASSWORD);
        testDataFactory.setRoomTeacher(room, teacher);
        contest = testDataFactory.createLiveContest("Marker Test Contest", room, 60);
        task = testDataFactory.createTask(contest, "M", "Marker Task");
    }

    @AfterEach
    void tearDown() {
        clearAllSessions();
    }

    @Test
    @DisplayName("Uploading a grader, then a checker, then a manager keeps bumping the marker")
    void uploadingTaskFilesBumpsTheMarkerEachTime() throws InterruptedException {
        long beforeGrader = readMarkerOrZero(task.getId());
        uploadGrader(task.getId(), "grader.cpp", "int f();");
        long afterGrader = readMarker(task.getId());
        assertThat(afterGrader).isGreaterThan(beforeGrader);

        sleepABit();
        uploadChecker(task.getId(), "checker.cpp", "int main() { return 0; }");
        long afterChecker = readMarker(task.getId());
        assertThat(afterChecker).isGreaterThan(afterGrader);

        sleepABit();
        uploadManager(task.getId(), "manager.cpp", "int main() { return 0; }");
        long afterManager = readMarker(task.getId());
        assertThat(afterManager).isGreaterThan(afterChecker);
    }

    @Test
    @DisplayName("Removing a task file bumps the marker")
    void removingATaskFileBumpsTheMarker() throws InterruptedException {
        uploadGrader(task.getId(), "grader.cpp", "int f();");
        long afterUpload = readMarker(task.getId());

        sleepABit();
        givenUser(teacher.getUsername(), PASSWORD)
                .when()
                .delete("/task/{taskId}/file/{kind}/{fileName}", task.getId(), "GRADER", "grader.cpp")
                .then()
                .statusCode(200);

        long afterDelete = readMarker(task.getId());
        assertThat(afterDelete).isGreaterThan(afterUpload);
    }

    @Test
    @DisplayName("Adding a testcase bumps the marker, and removing it bumps the marker again")
    void addingAndRemovingATestcaseBumpsTheMarker() throws InterruptedException {
        long beforeAdd = readMarkerOrZero(task.getId());

        givenUser(teacher.getUsername(), PASSWORD)
                .contentType(ContentType.MULTIPART)
                .multiPart("inputFile", "test1.in", "1".getBytes(StandardCharsets.UTF_8))
                .multiPart("outputFile", "test1.out", "1".getBytes(StandardCharsets.UTF_8))
                .when()
                .post("/task/{taskId}/testcase", task.getId())
                .then()
                .statusCode(200);

        long afterAdd = readMarker(task.getId());
        assertThat(afterAdd).isGreaterThan(beforeAdd);

        sleepABit();
        givenUser(teacher.getUsername(), PASSWORD)
                .when()
                .delete("/task/{taskId}/testcase/{testKey}", task.getId(), "1")
                .then()
                .statusCode(200);

        long afterRemove = readMarker(task.getId());
        assertThat(afterRemove).isGreaterThan(afterAdd);
    }

    @Test
    @DisplayName("Running a custom test bumps the marker for the shared custom-test task")
    void customTestRunBumpsTheMarker() {
        Task customTask = testDataFactory.createCustomTestTask();
        long before = readMarkerOrZero(customTask.getId());

        givenUser(teacher.getUsername(), PASSWORD)
                .body(Map.of(
                        "code", "int main() { return 0; }",
                        "language", "CPP",
                        "input", "1 2"
                ))
                .when()
                .post("/custom-test")
                .then()
                .statusCode(200);

        long after = readMarker(customTask.getId());
        assertThat(after).isGreaterThan(before);
    }

    private void uploadGrader(Long taskId, String fileName, String content) {
        upload("/task/{taskId}/graders", taskId, fileName, content);
    }

    private void uploadChecker(Long taskId, String fileName, String content) {
        upload("/task/{taskId}/checker", taskId, fileName, content);
    }

    private void uploadManager(Long taskId, String fileName, String content) {
        upload("/task/{taskId}/manager", taskId, fileName, content);
    }

    private void upload(String path, Long taskId, String fileName, String content) {
        givenUser(teacher.getUsername(), PASSWORD)
                .contentType(ContentType.MULTIPART)
                .multiPart("file", fileName, content.getBytes(StandardCharsets.UTF_8))
                .when()
                .post(path, taskId)
                .then()
                .statusCode(200);
    }

    private Path markerPath(Long taskId) {
        return Paths.get(taskDirectoryAddress.replace(":taskId", String.valueOf(taskId))).resolve("lastUpdate");
    }

    private long readMarker(Long taskId) {
        try {
            return Long.parseLong(Files.readString(markerPath(taskId)).trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private long readMarkerOrZero(Long taskId) {
        if (!Files.exists(markerPath(taskId))) {
            return 0;
        }
        return readMarker(taskId);
    }

    private void sleepABit() throws InterruptedException {
        Thread.sleep(10);
    }
}
