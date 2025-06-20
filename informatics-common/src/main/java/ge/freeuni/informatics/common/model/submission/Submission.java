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

    private Integer submissionMemory;

    @Column(nullable = false)
    private SubmissionStatus status;

    private Float score;

    private String compilationResult;

    private String compilationMessage;

    private Integer currentTest;

    @ElementCollection
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

    public Integer getSubmissionMemory() {
        return submissionMemory;
    }

    public void setSubmissionMemory(Integer submissionMemory) {
        this.submissionMemory = submissionMemory;
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

    public String getCompilationResult() {
        return compilationResult;
    }

    public void setCompilationResult(String compilationResult) {
        this.compilationResult = compilationResult;
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
