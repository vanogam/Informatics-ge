package ge.freeuni.informatics.common.model.submission;

import ge.freeuni.informatics.common.model.contest.Contest;
import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import jakarta.persistence.*;

import java.util.Date;
import java.util.List;

@Entity
public class Submission {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne
    private User user;

    @ManyToOne
    private Task task;

    @ManyToOne
    private Contest contest;

    @Column(nullable = false)
    private String fileName;

    @Column(nullable = false)
    private String language;

    private Date submissionTime;

    private Long time;

    private Integer memory;

    @Column(nullable = false)
    private SubmissionStatus status;

    private Float score;

    /**
     * The points this submission earned on each of the task's scoring units, encoded by
     * {@link SubtaskScores}. Null when there is no breakdown to record - a submission that never
     * compiled, or a task with more scoring units than are worth tracking.
     *
     * <p>Kept beside the total so that a contest scored by
     * {@link ge.freeuni.informatics.common.model.contest.ScoringType#SUBTASK_MAX} can merge this
     * submission into the contestant's running per-subtask maximum without re-scoring every
     * earlier submission.
     */
    @Column(name = "subtaskscores", length = 2000)
    private String subtaskScores;

    /**
     * Whether this submission is source code or the contestant's own output files. Null for the
     * submissions that predate the column, all of which are source.
     *
     * <p>For an OUTPUT submission {@link #fileName} names a directory holding one file per test
     * key rather than a single source file, and {@link #language} is not a {@code CodeLanguage}.
     */
    @Column(name = "submissionkind")
    private SubmissionKind kind = SubmissionKind.SOURCE;

    @Column(length = 1000)
    private String compilationMessage;

    private Integer currentTest;

    /**
     * Identifies the current judging run. Changes every time judging is started for this
     * submission, is carried on every message sent to a worker and echoed back on every
     * callback, so that results belonging to an abandoned run - one superseded by a re-judge -
     * can be told apart from the current one and dropped. Starts at 1.
     */
    @Column(nullable = false)
    private Integer judgeToken = 1;

    @ElementCollection(fetch = FetchType.LAZY)
    private List<SubmissionTestResult> submissionTestResults;

    private Long roomId;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public Contest getContest() {
        return contest;
    }

    public void setContest(Contest contest) {
        this.contest = contest;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public Date getSubmissionTime() {
        return submissionTime;
    }

    public void setSubmissionTime(Date submissionTime) {
        this.submissionTime = submissionTime;
    }

    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
    }

    public Integer getMemory() {
        return memory;
    }

    public void setMemory(Integer submissionMemory) {
        this.memory = submissionMemory;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public String getCompilationMessage() {
        return compilationMessage;
    }

    public void setCompilationMessage(String compilationMessage) {
        this.compilationMessage = compilationMessage;
    }

    public Integer getCurrentTest() {
        return currentTest;
    }

    public void setCurrentTest(Integer currentTest) {
        this.currentTest = currentTest;
    }

    /** Defaults to SOURCE, so a submission stored before the column existed reads correctly. */
    public SubmissionKind getKind() {
        return kind == null ? SubmissionKind.SOURCE : kind;
    }

    public void setKind(SubmissionKind kind) {
        this.kind = kind;
    }

    public boolean isOutputSubmission() {
        return getKind() == SubmissionKind.OUTPUT;
    }

    public String getSubtaskScores() {
        return subtaskScores;
    }

    public void setSubtaskScores(String subtaskScores) {
        this.subtaskScores = subtaskScores;
    }

    public Integer getJudgeToken() {
        return judgeToken;
    }

    public void setJudgeToken(Integer judgeToken) {
        this.judgeToken = judgeToken;
    }

    /**
     * Moves the submission onto a fresh judging run, so that anything still outstanding from the
     * previous one is recognisable as stale when it reports back.
     */
    public int nextJudgeToken() {
        judgeToken = judgeToken == null ? 1 : judgeToken + 1;
        return judgeToken;
    }

    public List<SubmissionTestResult> getSubmissionTestResults() {
        return submissionTestResults;
    }

    public Long getRoomId() {
        return roomId;
    }
    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public void setSubmissionTestResults(List<SubmissionTestResult> submissionTestResults) {
        this.submissionTestResults = submissionTestResults;
    }
}
