package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.AddTestcasesResult;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskInfo;
import ge.freeuni.informatics.common.model.task.TestCase;
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
        Hibernate.initialize(task.getTestCases());
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
            throw new InformaticsServerException("permissionDenied");
        }
        List<Contest> contests = contestRepository.findUpsolvingContests(roomId, new Date());
        List<TaskInfo> result = new ArrayList<>();
        for (Contest contest : contests) {
            for (Task task : contest.getTasks()) {
                TaskDTO taskDTO = TaskDTO.toDTO(task);
                ContestantResult contestantResult = contest.getUpsolvingStandings().stream()
                        .filter(res -> res.getContestantId() == currentUser.id())
                        .findFirst().orElse(null);
                if (contestantResult == null) {
                    result.add(new TaskInfo(taskDTO, 0F));
                } else {
                    result.add(new TaskInfo(taskDTO, contestantResult.getTaskResults().get(task.getCode()).getScore()));
                }
            }
        }
        return result;
    }

    @Override
    @MemberContestRestricted
    public Map<String, String> fillTaskNames(Long contestId) {
        Contest contest = contestRepository.getReferenceById(contestId);
        return contest.getTasks().stream().collect(Collectors.toMap(Task::getCode, Task::getTitle));
    }

    @Override
    @MemberContestRestricted
    public List<TaskInfo> getContestTasks(long contestId, int offset, int limit) throws InformaticsServerException {
        Contest contest = contestRepository.getReferenceById(contestId);
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        UserDTO currentUser = userManager.getAuthenticatedUser();
        if (!room.isMember(currentUser.id())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<TaskInfo> result = new ArrayList<>();
        for (Task task : contest.getTasks()) {
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
    @TeacherContestRestricted
    public TaskDTO addTask(long contestId, TaskDTO taskDTO) throws InformaticsServerException {
        Contest contest;
        try {
            contest = contestRepository.getReferenceById(contestId);
        } catch (EntityNotFoundException ex) {
            log.error("Contest with id {} not found", contestId, ex);
            throw new InformaticsServerException("contestNotFound");
        }
        if (!checkAddTaskPermission(contest)) {
            throw new InformaticsServerException("permissionDenied");
        }
        Task task = TaskDTO.fromDTO(taskDTO);
        if (task.getId() != null) {
            Task existingTask = taskRepository.findById(task.getId())
                    .orElseThrow(() -> new InformaticsServerException("taskNotFound"));
            if (!existingTask.getContest().getId().equals(contest.getId())) {
                throw new InformaticsServerException("taskNotInContest");
            }
            task.setTestCases(existingTask.getTestCases());
            task.setStatements(existingTask.getStatements());
        } else {
            task.setCode(FileUtils.getRandomFileName(10));
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
    public String getStatement(long taskId, Language language) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        Contest contest = task.getContest();
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        Long currentUser = userManager.getAuthenticatedUser().id();
        if (!room.isOpen() && !room.isMember(currentUser)) {
            throw new InformaticsServerException("permissionDenied");
        }
        if (!task.getStatements().containsKey(language)) {
            return null;
        }
        return task.getStatements().get(language);
    }

    @Override
    @TeacherTaskRestricted
    public void addStatement(long taskId, String statement, Language language) {
        Task task = taskRepository.getReferenceById(taskId);
        task.getStatements().put(language, statement);
        taskRepository.save(task);
    }

    @Override
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
        TestCase testCase = testcaseRepository.findFirstByTaskIdAndKey(taskId, testcaseKey);
        if (testCase == null) {
            log.error("Test case with id {} not found in task {}", testcaseKey, taskId);
            throw new InformaticsServerException("testCaseNotFound");
        }
        File zipFile = new File(FileUtils.buildPath(tempDirectoryAddress, task.getCode() + "_testcase_" + testcaseKey + ".zip"));
        if (zipFile.exists()) {
            boolean ignored = zipFile.delete();
        }
        try (FileOutputStream fos = new FileOutputStream(zipFile);
         ZipOutputStream zos = new ZipOutputStream(fos)) {
            addTestcaseToZip(zos, testCase);
        } catch (IOException e) {
            log.error("Error while creating zip for test case {}", testcaseKey, e);
            throw new InformaticsServerException("unexpectedException", e);
        }
        return zipFile;
    }

    @Override
    @TeacherTaskRestricted
    public File getTestcasesZip(long taskId) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        File zipFile = new File(FileUtils.buildPath(tempDirectoryAddress, task.getCode() + "_testcases.zip"));
        try (FileOutputStream fos = new FileOutputStream(zipFile);
            ZipOutputStream zos = new ZipOutputStream(fos)) {
            for (TestCase testCase : task.getTestCases()) {
                addTestcaseToZip(zos, testCase);
            }
        } catch (IOException e) {
            log.error("Error while creating zip for test cases of task {}", taskId, e);
            throw new InformaticsServerException("unexpectedException", e);
        }
        return zipFile;
    }

    private void addTestcaseToZip(ZipOutputStream zos, TestCase testCase) throws IOException {
            zos.putNextEntry(new ZipEntry(List.of(testCase.getInputFileAddress().split("/")).getLast()));
            Files.copy(Paths.get(testCase.getInputFileAddress()), zos);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry(List.of(testCase.getOutputFileAddress().split("/")).getLast()));
            Files.copy(Paths.get(testCase.getOutputFileAddress()), zos);
            Files.copy(Paths.get(testCase.getOutputFileAddress()), zos);
            zos.closeEntry();
    }

    @Override
    @TeacherTaskRestricted
    public AddTestcasesResult addTestcases(long taskId, byte[] testsZip) throws InformaticsServerException {
        File testsFolder;
        Task task = taskRepository.getReferenceById(taskId);
        try {
            testsFolder = new File(FileUtils.unzip(createTempZip(testsZip)));
        } catch (IOException ex) {
            log.error("Error occurred while creating tests zip.", ex);
            throw new InformaticsServerException("Error occurred while creating tests zip.");
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
                    throw new InformaticsServerException("unexpectedException", e);
                }
            }
        }
        return result;
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
    @TeacherTaskRestricted
    public void removeTestCase(long taskId, String testKey) throws InformaticsServerException {
        Task task = taskRepository.getReferenceById(taskId);
        TestCase testCase = testcaseRepository.findFirstByTaskIdAndKey(taskId, testKey);
        if (testCase == null) {
            log.error("Test case with key {} not found in task {}", testKey, taskId);
            throw new InformaticsServerException("testCaseAlreadyRemoved");
        }
        int index = task.getTestCases().indexOf(testCase);
        try {
            if (index == -1) {
                log.error("Test case with key {} not found in task {}", testKey, taskId);
                Files.delete(Path.of(testCase.getInputFileAddress()));
                Files.delete(Path.of(testCase.getOutputFileAddress()));
                testcaseRepository.delete(testCase);
                return;
            }

            Files.delete(Path.of(testCase.getInputFileAddress()));
            Files.delete(Path.of(testCase.getOutputFileAddress()));
            task.getTestCases().remove(index);
            testcaseRepository.delete(testCase);
            taskRepository.save(task);
        } catch (IOException e) {
            throw new InformaticsServerException("unexpectedError", e);
        }
    }

    private void addTestcaseLocal(Task task, byte[] inputContent, byte[] outputContent, String inputName, String outputName) throws InformaticsServerException {
        if (task.getTestCases() == null) {
            task.setTestCases(new ArrayList<>());
        }
        String testKey = getTestKey(inputName, outputName, task.getInputTemplate(), task.getOutputTemplate());
        deleteTestcaseIfExists(task, testKey);
        TestCase newTestCase = new TestCase();
        newTestCase.setKey(testKey);
        newTestCase.setTaskId(task.getId());
        insertTestcaseSorted(task, newTestCase);
        try {
            newTestCase.setInputFileAddress(createTestFile(inputName, String.valueOf(task.getId()), inputContent));
            newTestCase.setOutputFileAddress(createTestFile(outputName, String.valueOf(task.getId()), outputContent));
            newTestCase.setInputSnippet(new String(inputContent, 0, Math.min(1000, inputContent.length - 1), StandardCharsets.UTF_8));
            newTestCase.setOutputSnippet(new String(inputContent, 0, Math.min(1000, outputContent.length - 1), StandardCharsets.UTF_8));
        } catch (IOException e) {
            log.error("Error while creating test files for test {}", newTestCase.getKey(), e);
            throw new InformaticsServerException("unexpectedException", e);
        }
        testcaseRepository.save(newTestCase);
    }

    private void deleteTestcaseIfExists(Task task, String testKey) throws InformaticsServerException {
        for (int i = task.getTestCases().size() - 1; i >= 0; i --) {
            TestCase tc = task.getTestCases().get(i);
            if (testKey.equals(tc.getKey())) {
                try {
                    Files.delete(Path.of(tc.getInputFileAddress()));
                    Files.delete(Path.of(tc.getOutputFileAddress()));
                } catch (IOException e) {
                    log.error("Error while removing test case {}", tc.getKey(), e);
                    throw new InformaticsServerException("unexpectedException", e);
                }
                task.getTestCases().remove(i);
                testcaseRepository.delete(tc);
            }
        }
    }

    private void insertTestcaseSorted(Task task, TestCase newTestCase) {
        String testKey = newTestCase.getKey();
        int index = 0;
        for (TestCase tc : task.getTestCases()) {
            if (testKey.compareTo(tc.getKey()) < 0) {
                break;
            }
            index++;
        }
        task.getTestCases().add(index, newTestCase);
    }

    private String getTestKey(String inputName, String outputName, String inputTemplate, String outputTemplate) {
        String inputKey = getKeyFromTemplate(inputName, inputTemplate);
        String outputKey = getKeyFromTemplate(outputName, outputTemplate);
        if (inputKey == null || !inputKey.equals(outputKey)) {
            throw new IllegalArgumentException("invalidTestName");
        }
        return inputKey;
    }

    private boolean checkAddTaskPermission(Contest contest) throws InformaticsServerException {
        UserDTO user = userManager.getAuthenticatedUser();
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        if ("ADMIN".equals(user.role())) {
            return true;
        }
        return room.getTeachers().stream().anyMatch(u -> u.getId() == user.id());
    }

    private String createTestFile(String testName, String taskCode, byte[] fileContent) throws IOException, InformaticsServerException {
        String fileAddress = createTestFileAddress(testName, taskCode);
        File test = new File(fileAddress);
        if (!test.createNewFile()) {
            throw new InformaticsServerException("Could not create test file");
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
            throw new InformaticsServerException("Could not create tests zip");
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
            throw new InformaticsServerException("Could not create statement");
        }
        try (OutputStream outputStream = Files.newOutputStream(statementFile.toPath())) {
            outputStream.write(statement);
        }
        return fileAddress;
    }

    private String getStatementName(Language language) {
        return "statement_" + language.name() + ".pdf";
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
