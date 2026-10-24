package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Bumps the marker a worker's sandbox uses to decide whether its cached copy of a task
 * (tests, graders, manager, checker) is stale. Anything that adds, replaces or removes a
 * file a worker reads must call this, or workers keep judging against the previous copy.
 */
@Component
public class TaskVersionMarker {

    @Autowired
    Logger log;

    @Value("${ge.freeuni.informatics.Task.taskDirectoryAddress}")
    String taskDirectoryAddress;

    public void touchLastUpdate(long taskId) throws InformaticsServerException {
        Path root = Paths.get(taskRoot(taskId));
        try {
            Files.createDirectories(root);
            Files.writeString(root.resolve("lastUpdate"),
                    String.valueOf(System.currentTimeMillis()), StandardCharsets.UTF_8);
        } catch (IOException e) {
            log.error("Could not update lastUpdate marker for task {}", taskId, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
    }

    private String taskRoot(long taskId) {
        return taskDirectoryAddress.replace(":taskId", String.valueOf(taskId));
    }
}
