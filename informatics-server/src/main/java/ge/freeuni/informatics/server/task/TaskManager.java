package ge.freeuni.informatics.server.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.dto.TaskDTO;
import ge.freeuni.informatics.common.dto.UserDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.contest.ContestStatus;
import ge.freeuni.informatics.common.model.contest.ContestantResult;
import ge.freeuni.informatics.common.model.contestroom.ContestRoom;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskInfo;
import ge.freeuni.informatics.common.model.task.TestCase;
import ge.freeuni.informatics.judgeintegration.IJudgeIntegration;
import ge.freeuni.informatics.repository.contest.IContestRepository;
import ge.freeuni.informatics.repository.task.ITaskRepository;
import ge.freeuni.informatics.server.contestroom.IContestRoomManager;
import ge.freeuni.informatics.server.user.IUserManager;
import ge.freeuni.informatics.utils.FileUtils;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

@Component
public class TaskManager implements ITaskManager {

    @Autowired
    Logger log;

    @Autowired
    ITaskRepository taskRepository;

    @Autowired
    IContestRepository contestRepository;

    @Autowired
    IJudgeIntegration judgeIntegration;

    @Autowired
    IUserManager userManager;

    @Autowired
    IContestRoomManager roomManager;

    @Value("${ge.freeuni.informatics.Task.statementDirectoryAddress}")
    String statementsDirectoryAddress;

    @Value("${ge.freeuni.informatics.Task.testDirectoryAddress}")
    String testsDirectoryAddress;

    @Value("${ge.freeuni.informatics.Task.tempDirectoryAddress}")
    String tempDirectoryAddress;

    @Override
    public Task getTask(int taskId) {
        return taskRepository.getTask(taskId);
    }

   @Override
   @SuppressWarnings("unchecked")
    public List<String> getTaskNames(long contestId, String language) throws InformaticsServerException {
        Contest contest = null;
        try {
            contest = contestRepository.getContest(contestId);
        } catch (Exception ex) {
            throw new InformaticsServerException("contestNotFound");
        }
        return (List<String>) contest.getTasks().stream().map(task -> task.getTitle().getOrDefault(language, task.getCode()));
    }

    @Override
    public List<TaskInfo> getUpsolvingTasks(long roomId, int offset, int limit) throws InformaticsServerException {
        ContestRoom room = roomManager.getRoom(roomId);
        UserDTO currentUser = userManager.getAuthenticatedUser();
        if (!room.isMember(currentUser.getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<Contest> contests = contestRepository.getContests(roomId, null, Arrays.asList(ContestStatus.PAST), true, null, null);
        List<TaskInfo> result = new ArrayList<>();
        for (Contest contest : contests) {
            for (Task task : contest.getTasks()) {
                TaskDTO taskDTO = TaskDTO.toDTO(task);
                ContestantResult contestantResult = contest.getStandings().getContestantResult(currentUser.getId());
                if (contestantResult == null) {
                    result.add(new TaskInfo(taskDTO, 0F));
                } else {
                    result.add(new TaskInfo(taskDTO, contestantResult.getTaskScore(task.getCode())));
                }
            }
        }
        return result;
    }

   @Override
    public List<TaskInfo> getContestTasks(long contestId, int offset, int limit) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        UserDTO currentUser = userManager.getAuthenticatedUser();
        if (!room.isMember(currentUser.getId())) {
            throw new InformaticsServerException("permissionDenied");
        }
        List<TaskInfo> result = new ArrayList<>();
        for (Task task : contest.getTasks()) {
            TaskDTO taskDTO = TaskDTO.toDTO(task);
            ContestantResult contestantResult = contest.getStandings().getContestantResult(currentUser.getId());
            if (contestantResult == null) {
                result.add(new TaskInfo(taskDTO, 0F));
            } else {
                result.add(new TaskInfo(taskDTO, contestantResult.getTaskScore(task.getCode())));
            }
        }
        return result;
    }

    @Override
    public TaskDTO addTask(TaskDTO taskDTO, long contestId) throws InformaticsServerException {
        Contest contest = contestRepository.getContest(contestId);
        if (contest == null || !checkAddTaskPermission(contest)) {
            throw new InformaticsServerException("permissionDenied");
        }
        taskDTO.setContestId(contestId);
        Task task = TaskDTO.fromDTO(taskDTO);
        task = taskRepository.addTask(task);
        if (!contest.getTasks().contains(task)) {
            contest.getTasks().add(task);
            contestRepository.addContest(contest);
        }
        judgeIntegration.addTask(TaskDTO.toDTO(task));
        return TaskDTO.toDTO(task);
    }

    @Override
    public void removeTask(int taskId, long contest) {
    }

    @Override
    public File getStatement(int taskId, Language language) throws InformaticsServerException {
        Task task = taskRepository.getTask(taskId);
        Contest contest = contestRepository.getContest(task.getContestId());
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        Long currentUser = userManager.getAuthenticatedUser().getId();
        if (!room.isOpen() && !room.isMember(currentUser)) {
            throw new InformaticsServerException("permissionDenied");
        }
        if (!task.getStatements().containsKey(language.name())) {
            throw new InformaticsServerException("statementNotAvailable");
        }
        return new File(task.getStatements().get(language.name()));
    }

    @Override
    public void addStatement(int taskId, byte[] statement, Language language) throws InformaticsServerException {
        Task task = taskRepository.getTask(taskId);

        try {
            task.getStatements().put(language.name(), storeStatement(language, task.getCode(), statement));
            taskRepository.addTask(task);
        } catch (IOException e) {
            throw new InformaticsServerException("Error while storing statement.");
        }
    }

    @Override
    public void addTestcase(int taskId, int testIndex, byte[] inputContent, byte[] outputContent) throws InformaticsServerException {
        Task task = taskRepository.getTask(taskId);
        if (task == null) {
            throw new InformaticsServerException("Task does not exist");
        }
        addTestcaseLocal(task, testIndex, inputContent, outputContent);
        taskRepository.addTask(task);
        judgeIntegration.setTestcases(task);
    }

    @Override
    public void addTestcases(int taskId, byte[] testsZip) throws InformaticsServerException {
        String testsFolder;
        Task task = taskRepository.getTask(taskId);
        try {
            testsFolder = FileUtils.unzip(createTempZip(testsZip));
        } catch (IOException ex) {
            log.error("Error occurred while creating tests zip.", ex);
            throw new InformaticsServerException("Error occurred while creating tests zip.");
        }
        File testsDir = new File(testsFolder);
        String inputTemplate = task.getInputTemplate();
        String outputTemplate = task.getOutputTemplate();
        HashMap<Integer, String> inputs = new HashMap<>(), outputs = new HashMap<>();
        for (File file : Objects.requireNonNull(testsDir.listFiles())) {
            int testNum = getNumFromTemplate(file.getName(), inputTemplate);
            if (testNum != -1) {
                inputs.put(testNum, file.getPath());
            } else {
                testNum = getNumFromTemplate(file.getName(), outputTemplate);
                if (testNum != -1) {
                    outputs.put(testNum, file.getPath());
                }
            }
        }

        if (inputs.size() != outputs.size()) {
            throw new InformaticsServerException("InvalidTestData");
        }
        List<Integer> keys = new ArrayList<>(inputs.keySet());
        Collections.sort(keys);
        for (Integer index : keys) {
            if (!outputs.containsKey(index)) {
                throw new InformaticsServerException("InvalidTestData");
            }
            File inputFile = new File(inputs.get(index));
            File outputFile = new File(outputs.get(index));

            try {
                addTestcaseLocal(task, index, Files.readAllBytes(inputFile.toPath()), Files.readAllBytes(outputFile.toPath()));
            } catch (IOException ex) {
                log.error("Unexpected exception", ex);
            }
        }
        taskRepository.addTask(task);
        judgeIntegration.setTestcases(task);
    }

    @Override
    public void addManager(int taskId, byte[] manager) {

    }

    @Override
    public void removeManager(int taskId, String managerName) {

    }

    @Override
    public void removeTestCase(int taskId, long testcaseId) {

    }

    private void addTestcaseLocal(Task task, int testIndex, byte[] inputContent, byte[] outputContent) throws InformaticsServerException {
        TestCase testCase = new TestCase();
        testIndex --;
        if (task.getTestCases() == null) {
            task.setTestCases(new ArrayList<>());
        }
        if (testIndex > task.getTestCases().size()) {
            throw new InformaticsServerException("Can not insert into selected index");
        }
        String testName = FileUtils.getRandomFileName(15);
        try {
            testCase.setInputFileAddress(createTestFile(testName, task.getCode(), "input", inputContent));
            testCase.setOutputFileAddress(createTestFile(testName, task.getCode(), "output", outputContent));
        } catch (IOException ex) {
            log.error("Error occurred while creating test files.", ex);
            throw new InformaticsServerException("Error occurred while creating test files.");
        }
        if (task.getTestCases().size() == testIndex) {
            task.getTestCases().add(testCase);
        } else {
            task.getTestCases().set(testIndex, testCase);
        }
    }

    private boolean checkAddTaskPermission(Contest contest) throws InformaticsServerException {
        UserDTO user = userManager.getAuthenticatedUser();
        ContestRoom room = roomManager.getRoom(contest.getRoomId());
        return room.getTeachers().contains(user.getId());
    }

    private String createTestFile(String testName, String taskCode, String subFolder, byte[] fileContent) throws IOException, InformaticsServerException {
        String folder = FileUtils.buildPath(testsDirectoryAddress, taskCode, subFolder);
        Files.createDirectories(Paths.get(folder));
        String fileAddress = FileUtils.buildPath(folder, testName);
        File test = new File(fileAddress);
        if(!test.createNewFile()) {
            throw new InformaticsServerException("Could not create test file");
        }
        OutputStream outputStream = Files.newOutputStream(test.toPath());
        outputStream.write(fileContent);
        return fileAddress;
    }

    private String createTempZip(byte[] fileContent) throws IOException, InformaticsServerException {
        String folder = FileUtils.buildPath(tempDirectoryAddress, FileUtils.getRandomFileName(20));
        Files.createDirectories(Paths.get(folder));
        String fileName = FileUtils.getRandomFileName(5) + ".zip";
        String fileAddress = FileUtils.buildPath(folder, fileName);
        File testsZip = new File(fileAddress);
        if(!testsZip.createNewFile()) {
            throw new InformaticsServerException("Could not create tests zip");
        }
        OutputStream outputStream = Files.newOutputStream(testsZip.toPath());
        outputStream.write(fileContent);
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
        if(!statementFile.createNewFile()) {
            throw new InformaticsServerException("Could not create statement");
        }
        OutputStream outputStream = Files.newOutputStream(statementFile.toPath());
        outputStream.write(statement);
        return fileAddress;
    }

    private String getStatementName(Language language) {
        return "statement_" + language.name() + ".pdf";
    }

    private Integer getNumFromTemplate(String fileName, String template) {
        int numIndex = template.indexOf('*');
        if (!fileName.startsWith(template.substring(0, numIndex))) {
            return -1;
        }
        if (!fileName.endsWith(template.substring(numIndex + 1))) {
            return -1;
        }
        String numString = fileName.substring(numIndex, fileName.length() - (template.length() - numIndex - 1));
        return Integer.valueOf(numString);
    }
}
