package ge.freeuni.informatics.common.model.task;

public enum TaskType {
    BATCH("Batch"),
    /**
     * IOI/CMS style communication task. The submission is linked against the task's grader
     * sources and judged by the task's manager process, which it talks to over a FIFO pair.
     * New constants must be appended - the ordinal is what is persisted.
     */
    COMMUNICATION("Communication");

    private String code;

    TaskType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

}
