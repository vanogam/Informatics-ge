package ge.freeuni.informatics.common.model.task;

public enum TaskType {
    BATCH("Batch");

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
