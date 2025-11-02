package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.model.contest.Contest;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @ManyToOne
    Contest contest;

    @Column(unique = true, nullable = false)
    String code;

    String title;

    @ElementCollection
    @CollectionTable(name = "task_statements", joinColumns = @JoinColumn(name = "task_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "statement", length = 100000)
    Map<Language, String> statements;

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

    CheckerType checkerType;

    /**
     * Used to parse and number test case file names.
     */
    String inputTemplate;

    String outputTemplate;

    @OneToMany(cascade = CascadeType.ALL)
    List<Testcase> testcases;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    @Column(unique = true)
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getTitle() {
        return title;
    }


    public void setTitle(String title) {
        this.title = title;
    }

    public Map<Language, String> getStatements() {
        return statements;
    }

    public void setStatements(Map<Language, String> statements) {
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

    public CheckerType getCheckerType() {
        return checkerType;
    }

    public void setCheckerType(CheckerType checkerType) {
        this.checkerType = checkerType;
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

    public List<Testcase> getTestcases() {
        return testcases;
    }

    public void setTestCases(List<Testcase> testcases) {
        this.testcases = testcases;
    }

    @Transient

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Task) {
            return Objects.equals(id, ((Task) obj).id);
        }
        return false;
    }
}
