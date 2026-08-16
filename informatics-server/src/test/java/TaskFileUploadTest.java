
import ge.freeuni.informatics.common.dto.AddTaskFilesResult;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.task.TaskFile;
import ge.freeuni.informatics.common.model.task.TaskFileKind;
import ge.freeuni.informatics.repository.task.TaskFileRepository;
import ge.freeuni.informatics.server.task.TaskFileManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/** Grader and manager sources may be uploaded one by one, or as a zip. */
public class TaskFileUploadTest {

    @Mock private TaskFileRepository taskFileRepository;
    @Mock private Logger log;
    @InjectMocks private TaskFileManager manager;
    private Path root;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        root = Files.createTempDirectory("taskfiles");
        Field dir = TaskFileManager.class.getDeclaredField("taskDirectoryAddress");
        dir.setAccessible(true);
        dir.set(manager, root.toString() + "/:taskId");
        when(taskFileRepository.save(any())).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void storesASinglePlainSourceFile() throws Exception {
        AddTaskFilesResult result = manager.addTaskFiles(7L, TaskFileKind.GRADER,
                "int f();".getBytes(StandardCharsets.UTF_8), "grader.cpp");

        assertEquals(java.util.List.of("grader.cpp"), result.getSuccess());
        assertTrue(result.getRejected().isEmpty());
        assertEquals("int f();", Files.readString(root.resolve("7/graders/grader.cpp")));
    }

    @Test
    void storesAHeaderFileAndMarksItVisible() throws Exception {
        manager.addTaskFiles(7L, TaskFileKind.GRADER,
                "#pragma once".getBytes(StandardCharsets.UTF_8), "ballmachine.h");

        assertTrue(Files.exists(root.resolve("7/graders/ballmachine.h")));
    }

    @Test
    void stillUnpacksAZip() throws Exception {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ZipOutputStream zip = new ZipOutputStream(bytes)) {
            zip.putNextEntry(new ZipEntry("grader.cpp"));
            zip.write("a".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
            zip.putNextEntry(new ZipEntry("nested/ballmachine.h"));
            zip.write("b".getBytes(StandardCharsets.UTF_8));
            zip.closeEntry();
        }
        AddTaskFilesResult result = manager.addTaskFiles(7L, TaskFileKind.GRADER,
                bytes.toByteArray(), "graders.zip");

        assertEquals(2, result.getSuccess().size());
        assertTrue(Files.exists(root.resolve("7/graders/grader.cpp")));
        // nested paths are flattened to their leaf name
        assertTrue(Files.exists(root.resolve("7/graders/ballmachine.h")));
    }

    @Test
    void refusesAManagerUploadedAsAGrader() {
        byte[] source = "#include \"testlib.h\"\nint main(int argc, char** argv) { return 0; }"
                .getBytes(StandardCharsets.UTF_8);

        InformaticsServerException ex = assertThrows(InformaticsServerException.class,
                () -> manager.addTaskFiles(7L, TaskFileKind.GRADER, source, "manager.cpp"));

        assertEquals("evaluatorUploadedAsGrader", ex.getCode());
        assertFalse(Files.exists(root.resolve("7/graders/manager.cpp")));
    }

    @Test
    void stillAcceptsTheManagerInItsOwnSlot() throws Exception {
        byte[] source = "#include \"testlib.h\"\nint main() { return 0; }"
                .getBytes(StandardCharsets.UTF_8);

        manager.addTaskFiles(7L, TaskFileKind.MANAGER, source, "manager.cpp");

        assertTrue(Files.exists(root.resolve("7/manager/manager.cpp")));
    }

    @Test
    void stillAcceptsAnOrdinaryGrader() throws Exception {
        // A real grader defines main too, but never touches testlib.
        byte[] source = "#include \"ballmachine.h\"\nint main() { find_structure(1); }"
                .getBytes(StandardCharsets.UTF_8);

        manager.addTaskFiles(7L, TaskFileKind.GRADER, source, "grader.cpp");

        assertTrue(Files.exists(root.resolve("7/graders/grader.cpp")));
    }

    @Test
    void refusesFilesThatAreNotSource() throws Exception {
        AddTaskFilesResult result = manager.addTaskFiles(7L, TaskFileKind.MANAGER,
                new byte[]{0x7f, 'E', 'L', 'F'}, "manager");

        assertTrue(result.getSuccess().isEmpty());
        assertEquals(java.util.List.of("manager"), result.getRejected());
    }

    @Test
    void refusesPathTraversal() throws Exception {
        AddTaskFilesResult result = manager.addTaskFiles(7L, TaskFileKind.GRADER,
                "x".getBytes(StandardCharsets.UTF_8), "../../etc/passwd.cpp");

        assertTrue(result.getSuccess().isEmpty());
        assertFalse(Files.exists(root.resolve("etc")));
    }

    @Test
    void bumpsLastUpdateSoWorkersReload() throws Exception {
        manager.addTaskFiles(7L, TaskFileKind.GRADER,
                "x".getBytes(StandardCharsets.UTF_8), "grader.cpp");

        assertTrue(Files.exists(root.resolve("7/lastUpdate")));
        assertTrue(Long.parseLong(Files.readString(root.resolve("7/lastUpdate")).trim()) > 0);
    }
}
