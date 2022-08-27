package ge.freeuni.informatics.common.dto;


import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.submission.SubmissionTestResult;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class SubmissionDTO {

    private long id;

    private long cmsId;

    private long userId;

    private String text;

    private Float score;

    private int taskId;

    private long contestId;

    private String language;

    private Date submissionTime;

    private String compilationResult;

    private String compilationMessage;

    private List<SubmissionTestResult> results;

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

    public Float getScore() {
        return score;
    }

    public void setScore(Float score) {
        this.score = score;
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public long getContestId() {
        return contestId;
    }

    public void setContestId(long contestId) {
        this.contestId = contestId;
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

    public List<SubmissionTestResult> getResults() {
        return results;
    }

    public void setResults(List<SubmissionTestResult> results) {
        this.results = results;
    }

    public static SubmissionDTO toDTO(Submission submission) {
        SubmissionDTO submissionDTO = new SubmissionDTO();

        submissionDTO.setId(submission.getId());
        submissionDTO.setTaskId(submission.getTaskId());
        submissionDTO.setContestId(submission.getContestId());
        submissionDTO.setCmsId(submission.getCmsId());
        submissionDTO.setUserId(submission.getUserId());
        submissionDTO.setLanguage(submission.getLanguage());
        submissionDTO.setSubmissionTime(submission.getSubmissionTime());
        submissionDTO.setCompilationMessage(submission.getCompilationMessage());
        submissionDTO.setCompilationResult(submission.getCompilationResult());
        submissionDTO.setResults(submission.getSubmissionTestResultList().getSubmissionTestResults());
        submissionDTO.setText(submission.getText());
        submissionDTO.setScore(submission.getScore());

        return submissionDTO;
    }
    public static Submission fromDTO(SubmissionDTO submissionDTO) {
        Submission submission = new Submission();

        submission.setId(submissionDTO.getId());
        submission.setTaskId(submissionDTO.getTaskId());
        submission.setContestId(submissionDTO.getContestId());
        submission.setCmsId(submissionDTO.getCmsId());
        submission.setUserId(submissionDTO.getUserId());
        submission.setLanguage(submissionDTO.getLanguage());
        submission.setSubmissionTime(submissionDTO.getSubmissionTime());
        submission.setCompilationResult(submissionDTO.getCompilationResult());
        submission.setCompilationMessage(submissionDTO.getCompilationMessage());
        submission.setText(submissionDTO.getText());
        submission.setScore(submissionDTO.getScore());

        return submission;
    }

    public static List<SubmissionDTO> toDTOs(List<Submission> submissions) {
        List<SubmissionDTO> submissionDTOs = new ArrayList<>();
        for (Submission submission : submissions) {
            submissionDTOs.add(toDTO(submission));
        }
        return submissionDTOs;
    }
}
