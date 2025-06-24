package ge.informatics.sandbox;

import com.github.dockerjava.api.DockerClient;
import com.github.dockerjava.api.async.ResultCallback;
import com.github.dockerjava.api.command.ExecCreateCmdResponse;
import com.github.dockerjava.api.model.Frame;
import com.github.dockerjava.api.model.StreamType;
import com.github.dockerjava.core.DefaultDockerClientConfig;
import com.github.dockerjava.core.DockerClientImpl;
import com.github.dockerjava.httpclient5.ApacheDockerHttpClient;
import com.github.dockerjava.transport.DockerHttpClient;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class Utils {

    private static final int DEFAULT_TIMEOUT = 10000;

    /**
     * Compresses a file into a tar archive.
     *
     * @param file     The file to compress.
     * @param fileName The name of the file in the archive.
     * @return An InputStream containing the compressed file.
     */
    public static InputStream compressFile(File file, String fileName) throws IOException {
        return compressFile(file, fileName, null);
    }

    public static InputStream compressFile(File file, String fileName, String dirFilter) throws IOException {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
             TarArchiveOutputStream tarOutputStream = new TarArchiveOutputStream(byteArrayOutputStream)) {
            if (dirFilter == null || dirFilter.isEmpty()) {
                dirFilter = ".*";
            }
            addFileToTar(tarOutputStream, file, fileName, dirFilter);

            tarOutputStream.finish();
            return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
        }
    }

    private static void addFileToTar(TarArchiveOutputStream tarOutputStream, File file, String entryName, String dirFilter) throws IOException {
        if (file.isFile()) {
            TarArchiveEntry entry = new TarArchiveEntry(file, entryName);
            tarOutputStream.putArchiveEntry(entry);
            try (FileInputStream fileInputStream = new FileInputStream(file)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = fileInputStream.read(buffer)) > 0) {
                    tarOutputStream.write(buffer, 0, length);
                }
            }
            tarOutputStream.closeArchiveEntry();
        } else if (file.isDirectory()) {
            if (!file.getName().matches(dirFilter)) {
                return;
            }
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) {
                    addFileToTar(tarOutputStream, child, entryName + "/" + child.getName(), dirFilter);
                }
            }
        }
    }

    public static DockerClient createDockerClient() {
        DefaultDockerClientConfig config = DefaultDockerClientConfig.createDefaultConfigBuilder().build();
        DockerHttpClient httpClient = new ApacheDockerHttpClient.Builder()
                .dockerHost(config.getDockerHost())
                .sslConfig(config.getSSLConfig())
                .build();

        return DockerClientImpl.getInstance(config, httpClient);
    }

    /**
     * Executes a command in a Docker container synchronously.
     *
     * @param client      The Docker client.
     * @param containerId The ID of the container.
     * @param command     The command to execute.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting for the command to complete.
     */
    public static CommandResult executeCommandSync(DockerClient client, String containerId, String command) throws InterruptedException {
        return executeCommandSync(client, containerId, command, DEFAULT_TIMEOUT, null);
    }

    /**
     * Executes a command in a Docker container synchronously.
     *
     * @param client      The Docker client.
     * @param containerId The ID of the container.
     * @param command     The command to execute.
     * @throws IOException          If an I/O error occurs.
     * @throws InterruptedException If the thread is interrupted while waiting for the command to complete.
     */
    public static CommandResult executeCommandSync(DockerClient client, String containerId, String command, long timeMillis, Integer memoryLimit) throws InterruptedException {
        if (memoryLimit != null) {
            memoryLimit += 10 * 1024;
            float timeLimit = (500f + timeMillis) / 1000f;
            command = "ulimit -v " + memoryLimit + " && timeout " + timeLimit + "s " + command;
        }
        ExecCreateCmdResponse execCreateCmdResponse = client.execCreateCmd(containerId)
                .withCmd("sh", "-c", command)
                .withAttachStdout(true)
                .withAttachStderr(true)
                .exec();

        ByteArrayOutputStream stdout = new ByteArrayOutputStream();
        ByteArrayOutputStream stderr = new ByteArrayOutputStream();

        boolean completed = client.execStartCmd(execCreateCmdResponse.getId())
                .exec(new ResultCallback.Adapter<Frame>() {
                    @Override
                    public void onNext(Frame frame) {
                        try {
                            if (frame.getStreamType() == StreamType.STDERR) {
                                stderr.write(frame.getPayload());
                            } else if (frame.getStreamType() == StreamType.STDOUT) {
                                stdout.write(frame.getPayload());
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }).awaitCompletion(timeMillis, TimeUnit.MILLISECONDS);

        if (!completed) {
            return new CommandResult(true);
        }
        int exitCode = client.inspectExecCmd(execCreateCmdResponse.getId()).exec().getExitCode();
        return new CommandResult(exitCode, stdout, stderr);
    }

    public static void changePermissions(DockerClient client, String containerId, String path, String owner, String permissions) throws InterruptedException {
        executeCommandSync(client, containerId, "chown " + owner + ":" + owner + " " + path);
        executeCommandSync(client, containerId, "chmod " + permissions + " " + path);
    }

    public static String getExecProcessPid(DockerClient client, String containerId, String execCmd) throws Exception {
        // Run `ps` inside the container to list processes
        CommandResult result = Utils.executeCommandSync(client, containerId, "ps -eo pid,cmd");
        if (result.getExitCode() == 0) {
            String[] lines = result.getStdout().toString().split("\n");
            for (String line : lines) {
                if (line.contains(execCmd)) {
                    return line.trim().split("\\s+")[0]; // Extract PID
                }
            }
        }
        return null; // PID not found
    }

    public static void compressFileInContainer(String srcPath, String srcName, String destPath, DockerClient dockerClient, String containerId) {
        String command = String.format("tar -czf %s -C %s %s", destPath, srcPath, srcName);
        try {
            CommandResult result = Utils.executeCommandSync(dockerClient, containerId, command);
            if (result.getExitCode() != 0) {
                throw new RuntimeException("Error compressing file: " + result.getStderr().toString(StandardCharsets.UTF_8));
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Command execution interrupted", e);
        }
    }

    public static class CommandResult {
        private final int exitCode;
        private final boolean timeout;
        private final ByteArrayOutputStream stdout;
        private final ByteArrayOutputStream stderr;

        public CommandResult(boolean timeout) {
            this.exitCode = 0;
            this.stdout = null;
            this.stderr = null;
            this.timeout = timeout;
        }

        public CommandResult(int exitCode, ByteArrayOutputStream stdout, ByteArrayOutputStream stderr) {
            this.exitCode = exitCode;
            this.stdout = stdout;
            this.stderr = stderr;
            this.timeout = false;
        }


        public boolean isTimeout() {
            return timeout;
        }

        public int getExitCode() {
            return exitCode;
        }

        public ByteArrayOutputStream getStdout() {
            return stdout;
        }

        public ByteArrayOutputStream getStderr() {
            return stderr;
        }
    }
}
