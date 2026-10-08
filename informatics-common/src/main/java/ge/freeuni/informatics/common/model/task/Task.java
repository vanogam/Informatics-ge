package ge.freeuni.informatics.common.model.task;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.model.contest.Contest;
import jakarta.persistence.*;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

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

    @ElementCollection
    @CollectionTable(name = "task_editorials", joinColumns = @JoinColumn(name = "task_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "editorial", length = 100000)
    Map<Language, String> editorials;

    /**
     * Keyed by a free-form language name (e.g. "CPP", "JAVA", "PYTHON", or a custom name a
     * teacher typed in) rather than {@link Language}, which is the statement's natural language.
     */
    @ElementCollection
    @CollectionTable(name = "task_solutions", joinColumns = @JoinColumn(name = "task_id"))
    @MapKeyColumn(name = "language")
    @Column(name = "code", length = 100000)
    Map<String, String> solutions;

    /**
     * Teacher-controlled toggle. Even when true, a contestant only ever sees the editorial once
     * the task is in upsolving mode - never during a live contest.
     */
    @Column(name = "editorial_visible")
    Boolean editorialVisible = Boolean.FALSE;

    /** @see #editorialVisible */
    @Column(name = "solution_visible")
    Boolean solutionVisible = Boolean.FALSE;

    @ManyToMany
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    Set<Tag> tags;

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
     * Number of solution processes the manager drives. Only meaningful for
     * {@link TaskType#COMMUNICATION}; currently only 1 is supported.
     */
    @Column(name = "numprocesses")
    Integer numProcesses;

    /**
     * The kinds of submission the task accepts. A task normally takes source code and nothing
     * else; an output-only task takes the answers themselves - one file for a single test, or a
     * zip covering many - and a mixed task, IOI's "BatchAndOutput", takes either.
     *
     * <p>Nullable for the sake of tasks that predate the columns: absent reads as code-only,
     * which is what every existing task is.
     */
    @Column(name = "allowcodesubmission")
    Boolean allowCodeSubmission = Boolean.TRUE;

    @Column(name = "allowoutputsubmission")
    Boolean allowOutputSubmission = Boolean.FALSE;

    /**
     * Used to parse and number test case file names.
     */
    String inputTemplate;

    String outputTemplate;

    @OneToMany(cascade = CascadeType.ALL)
    List<Testcase> testcases;

    @Column(name = "taskorder")
    Integer order;

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

    public Map<Language, String> getEditorials() {
        return editorials;
    }

    public void setEditorials(Map<Language, String> editorials) {
        this.editorials = editorials;
    }

    public Map<String, String> getSolutions() {
        return solutions;
    }

    public void setSolutions(Map<String, String> solutions) {
        this.solutions = solutions;
    }

    /** Defaults to false, so a task saved before the column existed stays hidden. */
    public boolean isEditorialVisible() {
        return editorialVisible != null && editorialVisible;
    }

    public Boolean getEditorialVisible() {
        return editorialVisible;
    }

    public void setEditorialVisible(Boolean editorialVisible) {
        this.editorialVisible = editorialVisible;
    }

    /** Defaults to false, so a task saved before the column existed stays hidden. */
    public boolean isSolutionVisible() {
        return solutionVisible != null && solutionVisible;
    }

    public Boolean getSolutionVisible() {
        return solutionVisible;
    }

    public void setSolutionVisible(Boolean solutionVisible) {
        this.solutionVisible = solutionVisible;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTags(Set<Tag> tags) {
        this.tags = tags;
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

    public Integer getNumProcesses() {
        return numProcesses;
    }

    public void setNumProcesses(Integer numProcesses) {
        this.numProcesses = numProcesses;
    }

    /** Defaults to true, so a task saved before the column existed still accepts code. */
    public boolean isCodeSubmissionAllowed() {
        return allowCodeSubmission == null || allowCodeSubmission;
    }

    public Boolean getAllowCodeSubmission() {
        return allowCodeSubmission;
    }

    public void setAllowCodeSubmission(Boolean allowCodeSubmission) {
        this.allowCodeSubmission = allowCodeSubmission;
    }

    public boolean isOutputSubmissionAllowed() {
        return allowOutputSubmission != null && allowOutputSubmission;
    }

    public Boolean getAllowOutputSubmission() {
        return allowOutputSubmission;
    }

    public void setAllowOutputSubmission(Boolean allowOutputSubmission) {
        this.allowOutputSubmission = allowOutputSubmission;
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

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
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
