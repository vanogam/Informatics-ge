package ge.freeuni.informatics.server.customtest;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.repository.customtest.CustomTestRunRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.server.files.FileManager;
import ge.freeuni.informatics.server.user.IUserManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomTestService {

    private static final String CUSTOM_TASK_CODE = "_customTest";

    private final TaskRepository taskRepository;
    private final CustomTestRunRepository runRepository;
    private final IUserManager userManager;
    private final IJudgeIntegration judgeIntegration;

    @Value("${ge.freeuni.informatics.Task.testDirectoryAddress}")
    private String testsDirectoryAddress;

    private final FileManager fileManager;

    public CustomTestService(TaskRepository taskRepository,
                             CustomTestRunRepository runRepository,
                             IUserManager userManager,
                             IJudgeIntegration judgeIntegration,
                             FileManager fileManager) {
        this.taskRepository = taskRepository;
        this.runRepository = runRepository;
        this.userManager = userManager;
        this.judgeIntegration = judgeIntegration;
        this.fileManager = fileManager;
    }

    private Task getCustomTask() throws InformaticsServerException {
        Optional<Task> taskOpt = taskRepository.findAll()
                .stream()
                .filter(t -> CUSTOM_TASK_CODE.equals(t.getCode()))
                .findFirst();
        if (taskOpt.isEmpty()) {
            throw new InformaticsServerException("customTestTaskMissing");
        }
        return taskOpt.get();
    }

    @Transactional
    public String createRun(String sourceCode, CodeLanguage language, String input) throws InformaticsServerException {
        if (input != null && input.getBytes(StandardCharsets.UTF_8).length > 512 * 1024) {
            throw new InformaticsServerException("customTestInputTooLarge");
        }

        Task task = getCustomTask();

        long taskId = task.getId();
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String inputFileName = "input_custom_" + uuid + ".txt";
        String outputFileName = "output_custom_" + uuid + ".txt";

        CustomTestRun run = new CustomTestRun();
        run.setExternalKey(UUID.randomUUID().toString());
        try {
            run.setUserId(userManager.getAuthenticatedUser().id());
        } catch (InformaticsServerException ignored) {
            run.setUserId(null);
        }
        run.setTaskId(taskId);
        run.setLanguage(language.name());
        run.setInputFile(inputFileName);
        run.setOutputFile(outputFileName);
        run.setCreatedAt(new Date());
        run.setStatus("IN_QUEUE");
        // Save submission first so we have the file name before persisting the run
        String submissionFileName;
        try {
            submissionFileName = fileManager.saveTextSubmission(
                    taskId,
                    new Date(),
                    language,
                    sourceCode != null ? sourceCode : ""
            );
        } catch (Exception e) {
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
        run.setSubmissionFile(submissionFileName);
        run = runRepository.save(run);

        // Persist custom test input/output files so worker can access them
        try {
            // Save input and output files for the custom test under a separate custom-tests directory
            String testsDirString = testsDirectoryAddress.replace(":taskId", String.valueOf(taskId));
            Path testsDir = Paths.get(testsDirString);
            Path taskRootDir = testsDir.getParent() != null ? testsDir.getParent() : testsDir;
            Path customTestsDir = taskRootDir.resolve("custom-tests");
            Files.createDirectories(customTestsDir);

            // Save input and output files for the test
            Path inputPath = customTestsDir.resolve(inputFileName);
            Files.writeString(inputPath, input != null ? input : "", StandardCharsets.UTF_8);

            Path outputPath = customTestsDir.resolve(outputFileName);
            if (!Files.exists(outputPath)) {
                Files.writeString(outputPath, "", StandardCharsets.UTF_8);
            }

            // Update lastUpdate file so worker re-uploads task directory when needed
            Path lastUpdatePath = taskRootDir.resolve("lastUpdate");
            Files.writeString(lastUpdatePath, String.valueOf(System.currentTimeMillis()), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }

        // Send compilation task to worker via judge integration
        judgeIntegration.addCustomTest(task, run, language);

        return run.getExternalKey();
    }

    public CustomTestRun getRunByExternalKey(String key) throws InformaticsServerException {
        return runRepository.findByExternalKey(key)
                .orElseThrow(() -> new InformaticsServerException("customTestRunNotFound"));
    }
}

