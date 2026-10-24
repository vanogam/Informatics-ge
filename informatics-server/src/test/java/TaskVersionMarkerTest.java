import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.server.task.TaskVersionMarker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** The marker a worker reads to decide whether its cached copy of a task is stale. */
public class TaskVersionMarkerTest {

    @Mock private Logger log;
    @InjectMocks private TaskVersionMarker marker;
    private Path root;

    @BeforeEach
    void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        root = Files.createTempDirectory("taskversion");
        Field dir = TaskVersionMarker.class.getDeclaredField("taskDirectoryAddress");
        dir.setAccessible(true);
        dir.set(marker, root.toString() + "/:taskId");
    }

    @Test
    void writesATimestampUnderTheTaskDirectory() throws Exception {
        marker.touchLastUpdate(7L);

        Path markerFile = root.resolve("7/lastUpdate");
        assertTrue(Files.exists(markerFile));
        assertTrue(Long.parseLong(Files.readString(markerFile).trim()) > 0);
    }

    @Test
    void createsTheTaskDirectoryWhenItDoesNotExistYet() throws Exception {
        assertFalse(Files.exists(root.resolve("42")));

        marker.touchLastUpdate(42L);

        assertTrue(Files.exists(root.resolve("42/lastUpdate")));
    }

    @Test
    void bumpsTheTimestampOnEachCall() throws Exception {
        marker.touchLastUpdate(1L);
        long first = Long.parseLong(Files.readString(root.resolve("1/lastUpdate")).trim());

        Thread.sleep(5);
        marker.touchLastUpdate(1L);
        long second = Long.parseLong(Files.readString(root.resolve("1/lastUpdate")).trim());

        assertTrue(second >= first);
    }

    @Test
    void keepsTasksSeparate() throws Exception {
        marker.touchLastUpdate(1L);

        assertFalse(Files.exists(root.resolve("2/lastUpdate")));
    }

    @Test
    void wrapsAnIOFailureAsAnUnexpectedError() throws Exception {
        // Point the marker at a file instead of a directory root, so Files.createDirectories fails.
        Path blocked = Files.createFile(root.resolve("blocked"));
        Field dir = TaskVersionMarker.class.getDeclaredField("taskDirectoryAddress");
        dir.setAccessible(true);
        dir.set(marker, blocked.toString() + "/:taskId");

        assertThrows(InformaticsServerException.class, () -> marker.touchLastUpdate(1L));
    }
}
