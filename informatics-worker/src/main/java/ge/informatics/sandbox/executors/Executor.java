package ge.informatics.sandbox.executors;

import com.github.dockerjava.api.DockerClient;
import ge.informatics.sandbox.model.CompilationResult;
import ge.informatics.sandbox.model.Task;
import ge.informatics.sandbox.model.TestResult;

import java.io.IOException;

public interface Executor {

    String getCheckerName();

    String getSuffix();

    CompilationResult compileSubmission(DockerClient client, String containerId) throws IOException, InterruptedException;

    TestResult execute(DockerClient client, String containerId, Task task) throws IOException, InterruptedException;

}
