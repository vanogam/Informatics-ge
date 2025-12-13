package ge.freeuni.informatics.server.task;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.AddTestcasesResult;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.dto.TestcaseDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contest.TaskResult;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Statement;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskInfo;
import ge.freeuni.informatics.common.model.task.Testcase;
import ge.freeuni.informatics.repository.contest.ContestJpaRepository;
import ge.freeuni.informatics.repository.task.TaskRepository;
import ge.freeuni.informatics.repository.task.TestcaseRepository;
import ge.freeuni.informatics.server.annotation.MemberContestRestricted;
import ge.freeuni.informatics.server.annotation.MemberTaskRestricted;
import ge.freeuni.informatics.server.annotation.TeacherContestRestricted;
import ge.freeuni.informatics.server.annotation.TeacherTaskRestricted;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.FileUtils;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.transaction.annotation.Transactional;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Component
public class TaskManager implements ITaskManager {

    @Autowired
    Logger log;

    @Autowired
    TaskRepository taskRepository;

    @Autowired
    ContestJpaRepository contestRepository;

    @Autowired
    IUserManager userManager;

    @Autowired
    IContestRoomManager roomManager;

    @Autowired
    TestcaseRepository testcaseRepository;

    @Value("${ge.freeuni.informatics.Task.statementDirectoryAddress}")
    String statementsDirectoryAddress;

    @Value("${ge.freeuni.informatics.Task.testDirectoryAddress}")
    String testsDirectoryAddress;

    @Value("${ge.freeuni.informatics.Task.tempDirectoryAddress}")
    String tempDirectoryAddress;

    @Override
    @MemberTaskRestricted
    public Task getTask(long taskId) {
        Task task = taskRepository.getReferenceById(taskId);
        Hibernate.initialize(task.getTestcases());
        return task;
    }

    @Override
    @MemberContestRestricted
    public List<String> getTaskNames(long contestId, String language) throws InformaticsServerException {
        Contest contest;
        try {
            contest = contestRepository.getReferenceById(contestId);
        } catch (Exception ex) {
            throw new InformaticsServerException("contestNotFound");
        }
        return contest.getTasks().stream().map(Task::getTitle).toList();
    }

    @Override
    public List<TaskInfo> getUpsolvingTasks(long roomId, Integer offset, Integer limit) throws InformaticsServerException {
        ContestRoom room = roomManager.getRoom(roomId);
        UserDTO currentUser = userManager.getAuthenticatedUser();
        if (!room.isMember(currentUser.id())) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        List<Contest> contests = contestRepository.findUpsolvingContests(roomId, new Date());
        List<TaskInfo> result = new ArrayList<>();
        for (Contest contest : contests) {
            if (contest.getTasks() == null) {
                continue;
            }
            String contestName = contest.getName();
            for (Task task : contest.getTasks()) {
                TaskDTO taskDTO = TaskDTO.toDTO(task);
                ContestantResult contestantResult = contest.getUpsolvingStandings().stream()
                        .filter(res -> res.getContestantId() == currentUser.id())
                        .findFirst().orElse(null);
                Float score = null;
                if (contestantResult != null && contestantResult.getTaskResults() != null) {
                    TaskResult taskResult = contestantResult.getTaskResults().get(task.getCode());
                    if (taskResult != null) {
                        score = taskResult.getScore();
                    }
                }
                result.add(new TaskInfo(taskDTO, score, contestName));
            }
        }
        return result;
    }

    @Override
    @MemberContestRestricted
    public Map<String, String> fillTaskNames(Long contestId) {
        Contest contest = contestRepository.getReferenceById(contestId);
        return contest.getTasks().stream()
                .sorted(Comparator.comparing(task -> task.getOrder() != null ? task.getOrder() : 0))
                .collect(Collectors.toMap(
                        Task::getCode,
                        Task::getTitle,
                        (v1, v2) -> v1,
                        LinkedHashMap::new
                ));
    }

    @Override
    @MemberContestRestricted
    public List<TaskInfo> getContestTasks(long contestId, int offset, int limit) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        UserDTO currentUser = userManager.getAuthenticatedUser();
        if (!room.isMember(currentUser.id())) {
            throw InformaticsServerException.PERMISSION_DENIED;
        }
        List<TaskInfo> result = new ArrayList<>();
        List<Task> sortedTasks = contest.getTasks().stream()
                .sorted(Comparator.comparing(task -> task.getOrder() != null ? task.getOrder() : 0))
                .toList();
        for (Task task : sortedTasks) {
            TaskDTO taskDTO = TaskDTO.toDTO(task);
            ContestantResult contestantResult = contest.getStandings()
                    .stream()
                    .filter(res -> res.getContestantId() == currentUser.id())
                    .findFirst()
                    .orElse(null);
            if (contestantResult == null) {
                result.add(new TaskInfo(taskDTO, 0F));
            } else {
                result.add(new TaskInfo(taskDTO, contestantResult.getTaskResults().get(task.getCode()).getScore()));
            }
        }
        return result;
    }

    @Override
    @Transactional
    @TeacherContestRestricted
    public TaskDTO addTask(long contestId, TaskDTO taskDTO) throws InformaticsServerException {
        Contest contest;
        try {
            contest = contestRepository.getReferenceById(contestId);
        } catch (EntityNotFoundException ex) {
            log.error("Contest with id {} not found", contestId, ex);
            throw new InformaticsServerException("contestNotFound");
        }
        Task task = TaskDTO.fromDTO(taskDTO);
        if (task.getId() != null) {
            Task existingTask = taskRepository.findById(task.getId())
                    .orElseThrow(() -> new InformaticsServerException("taskNotFound"));
            if (!existingTask.getContest().getId().equals(contest.getId())) {
                throw new InformaticsServerException("taskNotInContest");
            }
            task.setTestCases(existingTask.getTestcases());
            task.setStatements(existingTask.getStatements());
            if (task.getOrder() == null) {
                task.setOrder(existingTask.getOrder());
            }
        } else {
            task.setCode(FileUtils.getRandomFileName(10));
            int taskCount = contest.getTasks() != null ? contest.getTasks().size() : 0;
            task.setOrder(taskCount + 1);
        }
        task.setContest(contest);
        task = taskRepository.save(task);
        if (!contest.getTasks().contains(task)) {
            contest.getTasks().add(task);
            contestRepository.save(contest);
        }
        return TaskDTO.toDTO(task);
    }

    @Override
    @TeacherTaskRestricted
    public void removeTask(long taskId, long testId) {
    }

    @Override
    @MemberTaskRestricted
    public Statement getStatement(long taskId, Language language) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        if (!task.getStatements().containsKey(language)) {
            return null;
        }
        String statementJson = task.getStatements().get(language);
        try {
            return new ObjectMapper().readValue(statementJson, Statement.class);
        } catch (JsonProcessingException e) {
            throw InformaticsServerException.INVALID_STATEMENT;
        }
    }

    @Override
    @MemberTaskRestricted
    public List<TestcaseDTO> getPublicTestcases(long taskId) {
        return testcaseRepository.findByTaskIdAndPublicTestcaseOrderByKey(taskId, true)
                .stream().map(tc -> new TestcaseDTO(tc.getKey(), tc.isPublicTestcase(), tc.getInputSnippet(), tc.getOutputSnippet())).toList();
    }

    @Override
    @TeacherTaskRestricted
    public void addStatement(long taskId, String statement, Language language) {
        Task task = taskRepository.getReferenceById(taskId);
        task.getStatements().put(language, statement);
        taskRepository.save(task);
    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public AddTestcasesResult addTestcase(long taskId, byte[] inputContent, byte[] outputContent, String inputName, String outputName) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        addTestcaseLocal(task, inputContent, outputContent, inputName, outputName);
        taskRepository.save(task);

        String testKey = getTestKey(inputName, outputName, task.getInputTemplate(), task.getOutputTemplate());
        AddTestcasesResult result = new AddTestcasesResult();
        result.getSuccess().add(testKey);
        return result;
    }

    @Override
    @TeacherTaskRestricted
    public File getTestcaseZip(long taskId, String testcaseKey) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        Testcase testcase = testcaseRepository.findFirstByTaskIdAndKey(taskId, testcaseKey);
        if (testcase == null) {
            log.error("Test case with id {} not found in task {}", testcaseKey, taskId);
            throw InformaticsServerException.TEST_NOT_FOUND;
        }
        File zipFile = new File(FileUtils.buildPath(tempDirectoryAddress, task.getCode() + "_testcase_" + testcaseKey + ".zip"));
        if (zipFile.exists()) {
            boolean ignored = zipFile.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(zipFile);
         ZipOutputStream zos = new ZipOutputStream(fos)) {
            addTestcaseToZip(zos, testcase);
        } catch (IOException e) {
            log.error("Error while creating zip for test case {}", testcaseKey, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
        return zipFile;
    }

    @Override
    @TeacherTaskRestricted
    public File getTestcasesZip(long taskId) throws InformaticsServerException {
        Task task;
        try {
            task = taskRepository.getReferenceById(taskId);
        } catch (EntityNotFoundException e) {
            log.error("Task with id {} not found", taskId, e);
            throw InformaticsServerException.TASK_NOT_FOUND;
        }
        File zipFile = new File(FileUtils.buildPath(tempDirectoryAddress, task.getCode() + "_testcases.zip"));
        try (FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (Testcase testcase : task.getTestcases()) {
                addTestcaseToZip(zos, testcase);
            }
        } catch (IOException e) {
            log.error("Error while creating zip for test cases of task {}", taskId, e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
        return zipFile;
    }

    private void addTestcaseToZip(ZipOutputStream zos, Testcase testcase) throws IOException {
            zos.putNextEntry(new ZipEntry(List.of(testcase.getInputFileAddress().split("/")).getLast()));
            Files.copy(Paths.get(testcase.getInputFileAddress()), zos);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry(List.of(testcase.getOutputFileAddress().split("/")).getLast()));
            Files.copy(Paths.get(testcase.getOutputFileAddress()), zos);
            Files.copy(Paths.get(testcase.getOutputFileAddress()), zos);
            zos.closeEntry();
    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public AddTestcasesResult addTestcases(long taskId, byte[] testsZip) throws InformaticsServerException {
        File testsFolder;
        Task task = taskRepository.getReferenceById(taskId);
        try {
            testsFolder = new File(FileUtils.unzip(createTempZip(testsZip)));
        } catch (IOException ex) {
            log.error("Error occurred while creating tests zip.", ex);
            throw InformaticsServerException.TEST_SAVE_EXCEPTION;
        }
        HashMap<String, String> inputs = new HashMap<>();
        HashMap<String, String> outputs = new HashMap<>();
        AddTestcasesResult result = new AddTestcasesResult();

        for (File file : Objects.requireNonNull(testsFolder.listFiles())) {
            String fileName = file.getName();
            result.getUnmatched().add(fileName);
            String inputKey = getKeyFromTemplate(fileName, task.getInputTemplate());
            String outputKey = getKeyFromTemplate(fileName, task.getOutputTemplate());
            if (inputKey != null) {
                inputs.put(inputKey, file.getAbsolutePath());
            } else if (outputKey != null) {
                outputs.put(outputKey, file.getAbsolutePath());
            } else {
                log.warn("File {} does not match input or output template", fileName);
            }
        }

        for (Map.Entry<String, String> entry : inputs.entrySet()) {
            if (outputs.containsKey(entry.getKey())) {
                String key = entry.getKey();
                String inputFile = entry.getValue();
                String outputFile = outputs.get(key);
                try {
                    byte[] inputContent = Files.readAllBytes(Paths.get(inputFile));
                    byte[] outputContent = Files.readAllBytes(Paths.get(outputFile));
                    String inputFileName = inputFile.substring(inputFile.lastIndexOf("/") + 1);
                    String outputFileName = outputFile.substring(outputFile.lastIndexOf("/") + 1);
                    addTestcaseLocal(task, inputContent, outputContent, inputFileName, outputFileName);
                    result.getSuccess().add(key);
                    result.getUnmatched().remove(inputFileName);
                    result.getUnmatched().remove(outputFileName);
                } catch (IOException e) {
                    log.error("Error while reading test files for {}", key, e);
                    throw InformaticsServerException.UNEXPECTED_ERROR;
                }
            }
        }
        return result;
    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public void setPublicTestcase(long taskId, String testcaseKey, boolean publicTestcase) throws InformaticsServerException {
        testcaseRepository.findFirstByKey(testcaseKey).ifPresent(tc -> {
            tc.setPublicTestcase(publicTestcase);
            testcaseRepository.save(tc);
        });
    }

    @Override
    @TeacherTaskRestricted
    public void addManager(long taskId, byte[] manager) {

    }

    @Override
    @TeacherTaskRestricted
    public void removeManager(long taskId, String managerName) {

    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public void removeTestCase(long taskId, String testKey) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        Testcase testcase = testcaseRepository.findFirstByTaskIdAndKey(taskId, testKey);
        if (testcase == null) {
            log.error("Test case with key {} not found in task {}", testKey, taskId);
            throw InformaticsServerException.TESTCASE_ALREADY_REMOVED;
        }
        int index = task.getTestcases().indexOf(testcase);
        try {
            if (index == -1) {
                log.error("Test case with key {} not found in task {}", testKey, taskId);
                Files.delete(Path.of(testcase.getInputFileAddress()));
                Files.delete(Path.of(testcase.getOutputFileAddress()));
                testcaseRepository.delete(testcase);
                return;
            }

            Files.delete(Path.of(testcase.getInputFileAddress()));
            Files.delete(Path.of(testcase.getOutputFileAddress()));
            task.getTestcases().remove(index);
            testcaseRepository.delete(testcase);
            taskRepository.save(task);
        } catch (IOException e) {
            log.error("Unexpected exception: ", e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
    }

    @Override
    @Transactional
    @TeacherTaskRestricted
    public void removeTestcases(long taskId, List<String> testKeys) throws InformaticsServerException {
        if (testKeys == null || testKeys.isEmpty()) {
            return;
        }
        
        for (String testKey : testKeys) {
            try {
                removeTestCase(taskId, testKey);
            } catch (InformaticsServerException ex) {
                if (ex == InformaticsServerException.TESTCASE_ALREADY_REMOVED) {
                    log.warn("Test case with key {} not found in task {}, skipping", testKey, taskId);
                } else {
                    throw ex;
                }
            }
        }
    }

    private void addTestcaseLocal(Task task, byte[] inputContent, byte[] outputContent, String inputName, String outputName) throws InformaticsServerException {
        if (task.getTestcases() == null) {
            task.setTestCases(new ArrayList<>());
        }
        String testKey = getTestKey(inputName, outputName, task.getInputTemplate(), task.getOutputTemplate());
        deleteTestcaseIfExists(task, testKey);
        Testcase newTestcase = new Testcase();
        newTestcase.setKey(testKey);
        newTestcase.setTaskId(task.getId());
        insertTestcaseSorted(task, newTestcase);
        try {
            newTestcase.setInputFileAddress(createTestFile(inputName, String.valueOf(task.getId()), inputContent));
            newTestcase.setOutputFileAddress(createTestFile(outputName, String.valueOf(task.getId()), outputContent));
            newTestcase.setInputSnippet(new String(inputContent, 0, Math.min(1000, inputContent.length), StandardCharsets.UTF_8));
            newTestcase.setOutputSnippet(new String(outputContent, 0, Math.min(1000, outputContent.length), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Error while creating test files for test {}", newTestcase.getKey(), e);
            throw InformaticsServerException.UNEXPECTED_ERROR;
        }
        testcaseRepository.save(newTestcase);
    }

    private void deleteTestcaseIfExists(Task task, String testKey) throws InformaticsServerException {
        for (int i = task.getTestcases().size() - 1; i >= 0; i --) {
            Testcase tc = task.getTestcases().get(i);
            if (testKey.equals(tc.getKey())) {
                try {
                    Files.delete(Path.of(tc.getInputFileAddress()));
                    Files.delete(Path.of(tc.getOutputFileAddress()));
                } catch (IOException e) {
                    log.error("Error while removing test case {}", tc.getKey(), e);
                    throw InformaticsServerException.UNEXPECTED_ERROR;
                }
                task.getTestcases().remove(i);
                testcaseRepository.delete(tc);
            }
        }
    }

    private void insertTestcaseSorted(Task task, Testcase newTestcase) {
        String testKey = newTestcase.getKey();
        int index = 0;
        for (Testcase tc : task.getTestcases()) {
            if (testKey.compareTo(tc.getKey()) < 0) {
                break;
            }
            index++;
        }
        task.getTestcases().add(index, newTestcase);
    }

    private String getTestKey(String inputName, String outputName, String inputTemplate, String outputTemplate) {
        String inputKey = getKeyFromTemplate(inputName, inputTemplate);
        String outputKey = getKeyFromTemplate(outputName, outputTemplate);
        if (inputKey == null || !inputKey.equals(outputKey)) {
            throw new IllegalArgumentException("invalidTestName");
        }
        return inputKey;
    }

    private String createTestFile(String testName, String taskCode, byte[] fileContent) throws IOException, InformaticsServerException {
        String fileAddress = createTestFileAddress(testName, taskCode);
        File test = new File(fileAddress);
        if (Files.exists(test.toPath())) {
            boolean ignored = test.delete();
        }
        if (!test.createNewFile()) {
            log.error("Error while creating test file {} for test {}", fileAddress, testName);
            throw InformaticsServerException.TEST_SAVE_EXCEPTION;
        }
        try (OutputStream outputStream = Files.newOutputStream(test.toPath())) {
            outputStream.write(fileContent);
        }
        return fileAddress;
    }

    private String createTestFileAddress(String testName, String taskCode) throws IOException {
        String folder = testsDirectoryAddress.replace(":taskId", taskCode);
        Files.createDirectories(Paths.get(folder));
        return FileUtils.buildPath(folder, testName);
    }

    private String createTempZip(byte[] fileContent) throws IOException, InformaticsServerException {
        String folder = FileUtils.buildPath(tempDirectoryAddress, FileUtils.getRandomFileName(20));
        Files.createDirectories(Paths.get(folder));
        String fileName = FileUtils.getRandomFileName(5) + ".zip";
        String fileAddress = FileUtils.buildPath(folder, fileName);
        File testsZip = new File(fileAddress);
        if (!testsZip.createNewFile()) {
            throw new InformaticsServerException("CouldNotCreateTestsZip");
        }
        try (OutputStream outputStream = Files.newOutputStream(testsZip.toPath())) {
            outputStream.write(fileContent);
        }
        return fileAddress;
    }

    private String storeStatement(Language lang, String taskCode, byte[] statement) throws IOException, InformaticsServerException {
        String folder = FileUtils.buildPath(statementsDirectoryAddress, taskCode);
        Files.createDirectories(Paths.get(folder));
        String fileAddress = FileUtils.buildPath(folder, getStatementName(lang));
        File statementFile = new File(fileAddress);
        if (statementFile.isFile()) {
            boolean ignored = statementFile.delete();
        }
        if (!statementFile.createNewFile()) {
            throw new InformaticsServerException("CouldNotCreateStatement");
        }
        try (OutputStream outputStream = Files.newOutputStream(statementFile.toPath())) {
            outputStream.write(statement);
        }
        return fileAddress;
    }

    private String getStatementName(Language language) {
        return "statement_" + language.name() + ".pdf";
    }

    @Override
    @Transactional
    @TeacherContestRestricted
    public void updateTaskOrder(long contestId, List<Long> taskIds) throws InformaticsServerException {
        Contest contest;
        contest = contestRepository.getReferenceById(contestId);
        
        for (int i = 0; i < taskIds.size(); i++) {
            Long taskId = taskIds.get(i);
            Task task = taskRepository.findById(taskId)
                    .orElseThrow(() -> InformaticsServerException.TASK_NOT_FOUND);
            if (!task.getContest().getId().equals(contest.getId())) {
                throw InformaticsServerException.TASK_NOT_IN_CONTEST;
            }
            task.setOrder(i + 1);
            taskRepository.save(task);
        }
    }

    private String getKeyFromTemplate(String fileName, String template) {
        Pattern SPECIAL_REGEX_CHARS = Pattern.compile("[{}()\\[\\].+?^$\\\\|]");
        template = SPECIAL_REGEX_CHARS.matcher(template).replaceAll("\\\\$0");
        template = template.replace("*", "(.*)");

        Pattern pattern = Pattern.compile(template);
        Matcher matcher = pattern.matcher(fileName);

        if (matcher.matches()) {
            return matcher.group(1);
        }
        return null;
    }
}
