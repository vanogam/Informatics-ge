package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.task.TaskScoreType;
import ge.freeuni.informatics.common.model.task.TaskType;
import ge.freeuni.informatics.common.model.task.TestCase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskDTO {

    Integer id;

    Long contestId;

    String code;

    Map<String, String> title = new HashMap<>();

    String configAddress;

    TaskType taskType;

    TaskScoreType taskScoreType;

    /**
     * Describes how to distribute score to test cases.
     * For formatting info, see TaskScoreType class.
     */
    String taskScoreParameter;

    Integer timeLimitMillis;

    Integer memoryLimitMB;

    /**
     * Used to parse and number test case file names.
     */
    String inputTemplate;

    String outputTemplate;

    List<TestCase> testCases;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Map<String, String> getTitle() {
        return title;
    }

    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    public String getConfigAddress() {
        return configAddress;
    }

    public void setConfigAddress(String configAddress) {
        this.configAddress = configAddress;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public TaskScoreType getTaskScoreType() {
        return taskScoreType;
    }

    public void setTaskScoreType(TaskScoreType taskScoreType) {
        this.taskScoreType = taskScoreType;
    }

    public String getTaskScoreParameter() {
        return taskScoreParameter;
    }

    public void setTaskScoreParameter(String taskScoreParameter) {
        this.taskScoreParameter = taskScoreParameter;
    }

    public Integer getTimeLimitMillis() {
        return timeLimitMillis;
    }

    public void setTimeLimitMillis(Integer timeLimitMillis) {
        this.timeLimitMillis = timeLimitMillis;
    }

    public Integer getMemoryLimitMB() {
        return memoryLimitMB;
    }

    public void setMemoryLimitMB(Integer memoryLimitMB) {
        this.memoryLimitMB = memoryLimitMB;
    }

    public String getInputTemplate() {
        return inputTemplate;
    }

    public void setInputTemplate(String inputTemplate) {
        this.inputTemplate = inputTemplate;
    }

    public String getOutputTemplate() {
        return outputTemplate;
    }

    public void setOutputTemplate(String outputTemplate) {
        this.outputTemplate = outputTemplate;
    }

    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public static Task fromDTO(TaskDTO taskDTO) {
        Task task = new Task();

        task.setTaskType(taskDTO.getTaskType());
        task.setContestId(task.getContestId());
        task.setTaskScoreParameter(taskDTO.getTaskScoreParameter());
        task.setTaskScoreType(taskDTO.getTaskScoreType());
        task.setId(taskDTO.getId());
        task.setCode(taskDTO.getCode());
        task.setConfigAddress(taskDTO.getConfigAddress());
        task.setTestCases(taskDTO.getTestCases());
        task.setInputTemplate(taskDTO.getInputTemplate());
        task.setOutputTemplate(taskDTO.getOutputTemplate());
        task.setMemoryLimitMB(taskDTO.getMemoryLimitMB());
        task.setTimeLimitMillis(taskDTO.getTimeLimitMillis());
        task.setTitle(taskDTO.getTitle());

        return task;
    }

    public static List<Task> fromDTOs(List<TaskDTO> taskDTOs) {
        List<Task> tasks = new ArrayList<>();

        for (TaskDTO taskDTO : taskDTOs) {
            tasks.add(TaskDTO.fromDTO(taskDTO));
        }
        return tasks;
    }

    public static TaskDTO toDTO(Task task) {
        TaskDTO taskDTO = new TaskDTO();

        taskDTO.setTaskType(task.getTaskType());
        taskDTO.setContestId(task.getContestId());
        taskDTO.setTaskScoreParameter(task.getTaskScoreParameter());
        taskDTO.setTaskScoreType(task.getTaskScoreType());
        taskDTO.setId(task.getId());
        taskDTO.setCode(task.getCode());
        taskDTO.setConfigAddress(task.getConfigAddress());
        taskDTO.setTestCases(task.getTestCases());
        taskDTO.setInputTemplate(task.getInputTemplate());
        taskDTO.setOutputTemplate(task.getOutputTemplate());
        taskDTO.setMemoryLimitMB(task.getMemoryLimitMB());
        taskDTO.setTimeLimitMillis(task.getTimeLimitMillis());
        taskDTO.setTitle(task.getTitle());

        return taskDTO;
    }

    public static List<TaskDTO> toDTOs(List<Task> tasks) {
        List<TaskDTO> taskDTOs = new ArrayList<>();

        for (Task task : tasks) {
            taskDTOs.add(TaskDTO.toDTO(task));
        }
        return taskDTOs;
    }

}
