package ge.freeuni.informatics.common.model.task;

public record Statement(
        String title,
        String statement,
        String inputInfo,
        String outputInfo,
        String notes
) {
}
