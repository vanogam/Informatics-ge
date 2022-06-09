package ge.freeuni.informatics.model.entity.task;

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
