package ge.freeuni.informatics.model.dto;


import ge.freeuni.informatics.model.entity.submission.Submission;

import java.util.Date;

public class SubmissionDTO {

    private long id;

    private long cmsId;

    private long userId;

    private long taskId;

    private String language;

    private Date submissionTime;

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

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public long getTaskId() {
        return taskId;
    }

    public void setTaskId(long taskId) {
        this.taskId = taskId;
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

    public static SubmissionDTO toDTO(Submission submission) {
        SubmissionDTO submissionDTO = new SubmissionDTO();

        submissionDTO.setId(submission.getId());
        submissionDTO.setTaskId(submission.getTaskId());
        submissionDTO.setCmsId(submission.getCmsId());
        submissionDTO.setUserId(submission.getUserId());
        submissionDTO.setLanguage(submission.getLanguage());
        submissionDTO.setSubmissionTime(submission.getSubmissionTime());

        return submissionDTO;
    }
    public static Submission fromDTO(SubmissionDTO submissionDTO) {
        Submission submission = new Submission();

        submission.setId(submissionDTO.getId());
        submission.setTaskId(submissionDTO.getTaskId());
        submission.setCmsId(submissionDTO.getCmsId());
        submission.setUserId(submissionDTO.getUserId());
        submission.setLanguage(submissionDTO.getLanguage());
        submission.setSubmissionTime(submissionDTO.getSubmissionTime());

        return submission;
    }
}
