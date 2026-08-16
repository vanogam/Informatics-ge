package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.model.task.TaskFile;
import ge.freeuni.informatics.common.model.task.TaskFileKind;

import java.util.Date;
import java.util.List;

public record TaskFileDTO(
        Long id,
        TaskFileKind kind,
        String fileName,
        Long sizeBytes,
        Date uploadedAt,
        boolean visibleToContestants
) {

    public static TaskFileDTO toDTO(TaskFile taskFile) {
        return new TaskFileDTO(
                taskFile.getId(),
                taskFile.getKind(),
                taskFile.getFileName(),
                taskFile.getSizeBytes(),
                taskFile.getUploadedAt(),
                taskFile.isVisibleToContestants()
        );
    }

    public static List<TaskFileDTO> toDTOs(List<TaskFile> taskFiles) {
        return taskFiles.stream().map(TaskFileDTO::toDTO).toList();
    }
}