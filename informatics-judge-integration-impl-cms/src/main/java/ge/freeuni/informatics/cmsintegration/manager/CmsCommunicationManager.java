package ge.freeuni.informatics.cmsintegration.manager;

import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TestCase;
import ge.freeuni.informatics.utils.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.zeroturnaround.zip.ZipUtil;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

@Service
public class CmsCommunicationManager implements ICmsCommunicationManager {

    final Logger log;

    @Value("${ge.freeuni.informatics.cmsintegration.pythonCommandExecutable}")
    private String pythonCommandExecutable;

    @Value("${ge.freeuni.informatics.cmsintegration.commandOutput}")
    private String commandOutput;

    @Value("${ge.freeuni.informatics.cmsintegration.commandErrorOutput}")
    private String commandErrorOutput;

    @Value("${ge.freeuni.informatics.cmsintegration.submissionPythonFileAddress}")
    private String submissionFileAddress;

    @Value("${ge.freeuni.informatics.cmsintegration.taskPythonFileAddress}")
    private String taskFileAddress;

    @Value("${ge.freeuni.informatics.cmsintegration.testcasePythonFileAddress}")
    private String testcaseFileAddress;

    @Value("${ge.freeuni.informatics.cmsintegration.informaticsCmsUser}")
    private String informaticsCmsUser;

    @Value("${ge.freeuni.informatics.Task.tempDirectoryAddress}")
    private String tempDirectory;
    @Value("${ge.freeuni.informatics.cmsintegration.contestName}")
    private String contestName;

    @Autowired
    public CmsCommunicationManager(Logger log) {
        this.log = log;
    }

    @Override
    public void addSubmission(Submission submission, Task task) throws InformaticsServerException {

        ProcessBuilder processBuilder = new ProcessBuilder().command(
                pythonCommandExecutable, submissionFileAddress,
                "-c", "1",
                "-f", constructFileName(submission, task),
                "-I", String.valueOf(submission.getId()),
                informaticsCmsUser,
                task.getCode());
        try {
            runProcess(processBuilder);
        } catch (IOException ex) {
            log.error("Error during executing add submission command.", ex);
            throw new InformaticsServerException("AddSubmissionException", ex);
        }
    }


    @Override
    public void addTask(Task task) throws InformaticsServerException {
        ProcessBuilder processBuilder = new ProcessBuilder().command(
                pythonCommandExecutable, taskFileAddress,
                "-u",
                "-S",
                "-c", "1",
                "-n", task.getCode(),
                "-I", String.valueOf(task.getId()),
                task.getConfigAddress()
        );
        try {
            runProcess(processBuilder);
        } catch (IOException ex) {
            log.error("Error during executing add task command.", ex);
            throw new InformaticsServerException("AddTaskException", ex);
        }
    }

    @Override
    public void addTestcases(Task task) throws InformaticsServerException {
        Pair<String, String> testTemplate = getTestcaseTemplate(task);
        String zipAddress = createTestsZip(task.getTestCases(), testTemplate);
        ProcessBuilder processBuilder = new ProcessBuilder().command(
                pythonCommandExecutable, testcaseFileAddress,
                "-p",
                "-o",
                "-c", contestName,
                task.getCode(),
                zipAddress,
                testTemplate.getFirst(),
                testTemplate.getSecond()
        );

        try {
            runProcess(processBuilder);
        } catch (IOException ex) {
            log.error("Error during executing add testcases command.", ex);
            throw new InformaticsServerException("AddTestcasesException", ex);
        }
    }

    private String constructFileName(Submission submission, Task task) {
        return task.getCode() + ".%l:" + submission.getFileName();
    }

    private String createTestsZip(List<TestCase> testCases, Pair<String, String> testTemplate) throws InformaticsServerException {
        String folderName = FileUtils.getRandomFileName(10);
        String testFolderPath = FileUtils.buildPath(tempDirectory, folderName, "tests");
        File testFolder = new File(testFolderPath);
        if (!testFolder.mkdirs()) {
            throw new InformaticsServerException("Could not create temp folder");
        }
        int index = 1;
        for (TestCase testCase : testCases) {
            File inputFileFrom = new File(testCase.getInputFileAddress());
            File outputFileFrom = new File(testCase.getOutputFileAddress());

            File inputFileTo = new File(FileUtils.buildPath(testFolderPath,
                    getTestcaseFilename(testCases.size(), index, true, testTemplate)));
            File outputFileTo = new File(FileUtils.buildPath(testFolderPath,
                    getTestcaseFilename(testCases.size(), index, false, testTemplate)));

            try {
                Files.copy(inputFileFrom.toPath(), inputFileTo.toPath());
                Files.copy(outputFileFrom.toPath(), outputFileTo.toPath());
            } catch (IOException ex) {
                log.error("Could not copy tests", ex);
                throw new InformaticsServerException("Could not copy tests", ex);
            }
            index ++;
        }
        String zipPath = FileUtils.buildPath(tempDirectory, folderName, "tests.zip");
        ZipUtil.pack(testFolder, new File(zipPath));
        return zipPath;
    }

    private Pair<String, String> getTestcaseTemplate(Task task) {
        return Pair.of(task.getCode() + ".*.in", task.getCode() + ".*.sol");
    }

    private String getTestcaseFilename(int totalTestcases, int index, boolean isInput, Pair<String, String> testTemplate) {
        String template;
        if (isInput) {
            template = testTemplate.getFirst();
        } else {
            template = testTemplate.getSecond();
        }
        StringBuilder num = new StringBuilder(String.valueOf(index));
        int size = String.valueOf(totalTestcases).length();
        while (num.length() < size) {
            num.insert(0, "0");
        }
        return template.replace("*", num.toString());
    }

    private void runProcess(ProcessBuilder processBuilder) throws IOException, InformaticsServerException {
        try {
            int exitCode = processBuilder.redirectOutput(new File(commandOutput)).redirectError(new File(commandErrorOutput)).start().waitFor();
            if (exitCode != 0) {
                throw new InformaticsServerException("cmsException");
            }
        } catch (InterruptedException e) {
            log.error("Error during executing command", e);
            throw new InformaticsServerException("cmsException");
        }
    }
}
