package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.CodeLanguage;

public class TextSubmitRequest {

    private Integer contestId;

    private Integer taskId;

    private String submissionText;

    private CodeLanguage language;

    public Integer getContestId() {
        return contestId;
    }

    public void setContestId(Integer contestId) {
        this.contestId = contestId;
    }

    public Integer getTaskId() {
        return taskId;
    }

    public void setTaskId(Integer taskId) {
        this.taskId = taskId;
    }

    public String getSubmissionText() {
        return submissionText;
    }

    public void setSubmissionText(String submissionText) {
        this.submissionText = submissionText;
    }

    public CodeLanguage getLanguage() {
        return language;
    }

    public void setLanguage(CodeLanguage language) {
        this.language = language;
    }
}
