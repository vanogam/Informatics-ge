package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.task.TaskScoreType;
import ge.freeuni.informatics.common.model.task.TaskType;
import java.util.Map;

public class AddTaskRequest {

    private Integer contestId;

    private String code;

    private Map<String, String> title;

    private TaskType taskType;

    private TaskScoreType taskScoreType;

    /**
     * Describes how to distribute score to test cases.
     * For formatting info, see TaskScoreType class.
     */
    private String taskScoreParameter;

    private Integer timeLimitMillis;

    private Integer memoryLimitMB;

    private String inputTemplate;

    private String outputTemplate;

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
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
}
