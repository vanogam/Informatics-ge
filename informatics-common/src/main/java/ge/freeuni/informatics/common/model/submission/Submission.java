package ge.freeuni.informatics.common.model.submission;

import ge.freeuni.informatics.common.model.task.Task;
import ge.freeuni.informatics.common.model.user.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Submission {

    private long id;

    private long cmsId;

    private long userId;

    private int taskId;

    private long contestId;

    private long roomId;

    private String fileName;

    private String language;

    private Date submissionTime;

    private SubmissionStatus status;

    private Float score;

    private String compilationResult;

    private String compilationMessage;

    private SubmissionTestResultList submissionTestResultList;

    @Id
    @GeneratedValue
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getCmsId() {
        return cmsId;
    }

    public void setCmsId(long cmsId) {
        this.cmsId = cmsId;
    }

    @OneToOne(targetEntity = User.class)
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @OneToOne(targetEntity = Task.class)
    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public long getContestId() {
        return contestId;
    }

    public void setContestId(long contestId) {
        this.contestId = contestId;
    }

    public long getRoomId() {
        return roomId;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
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

    @Type(type = SubmissionTestResultListType.TYPE)
    @Column(length = Integer.MAX_VALUE)
    public SubmissionTestResultList getSubmissionTestResultList() {
        return submissionTestResultList;
    }

    public void setSubmissionTestResultList(SubmissionTestResultList submissionTestResultList) {
        this.submissionTestResultList = submissionTestResultList;
    }
}
