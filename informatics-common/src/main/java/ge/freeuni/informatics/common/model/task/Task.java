package ge.freeuni.informatics.common.model.task;

import javax.persistence.*;
import java.util.List;
import java.util.Map;

@Entity
public class Task {

    Integer id;

    Integer judgeId;

    Long contestId;

    String code;

    Map<String, String> title;

    Map<String, String> statements;

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

    @Id
    @GeneratedValue
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getJudgeId() {
        return judgeId;
    }

    public void setJudgeId(Integer judgeId) {
        this.judgeId = judgeId;
    }

    public Long getContestId() {
        return contestId;
    }

    public void setContestId(Long contestId) {
        this.contestId = contestId;
    }

    @Column(unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @ElementCollection
    public Map<String, String> getTitle() {
        return title;
    }


    public void setTitle(Map<String, String> title) {
        this.title = title;
    }

    @ElementCollection
    public Map<String, String> getStatements() {
        return statements;
    }

    public void setStatements(Map<String, String> statements) {
        this.statements = statements;
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

    @OneToMany(cascade = CascadeType.ALL)
    public List<TestCase> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }
}
