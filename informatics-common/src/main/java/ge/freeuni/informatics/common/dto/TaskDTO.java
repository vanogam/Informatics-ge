package ge.freeuni.informatics.common.dto;

import ge.freeuni.informatics.common.Language;
import ge.freeuni.informatics.common.model.task.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record TaskDTO(
        Long id,
        Long contestId,
        String code,
        String title,
        TaskType taskType,
        TaskScoreType taskScoreType,
        String taskScoreParameter,
        Integer timeLimitMillis,
        Integer memoryLimitMB,
        CheckerType checkerType,
        String inputTemplate,
        String outputTemplate,
        Map<Language, String> statements,
        List<String> testCases
) {

    public static Task fromDTO(TaskDTO taskDTO) {
        Task task = new Task();

        task.setTaskType(taskDTO.taskType());
        task.setTaskScoreParameter(taskDTO.taskScoreParameter());
        task.setTaskScoreType(taskDTO.taskScoreType());
        task.setId(taskDTO.id());
        task.setCode(taskDTO.code());
        task.setCheckerType(taskDTO.checkerType());
        task.setInputTemplate(taskDTO.inputTemplate());
        task.setOutputTemplate(taskDTO.outputTemplate());
        task.setMemoryLimitMB(taskDTO.memoryLimitMB());
        task.setTimeLimitMillis(taskDTO.timeLimitMillis());
        task.setTitle(taskDTO.title());

        return task;
    }

    public static List<Task> fromDTOs(List<TaskDTO> taskDTOs) {
        List<Task> tasks = new ArrayList<>();

        for (TaskDTO taskDTO : taskDTOs) {
            tasks.add(TaskDTO.fromDTO(taskDTO));
        }
        return tasks;
    }

    public static TaskDTO toDTO(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getContest().getId(),
                task.getCode(),
                task.getTitle(),
                task.getTaskType(),
                task.getTaskScoreType(),
                task.getTaskScoreParameter(),
                task.getTimeLimitMillis(),
                task.getMemoryLimitMB(),
                task.getCheckerType(),
                task.getInputTemplate(),
                task.getOutputTemplate(),
                task.getStatements(),
                task.getTestCases() == null ? null : task.getTestCases().stream()
                        .map(TestCase::getKey)
                        .toList()
        );
    }

    public static List<TaskDTO> toDTOs(List<Task> tasks) {
        List<TaskDTO> taskDTOs = new ArrayList<>();

        for (Task task : tasks) {
            taskDTOs.add(TaskDTO.toDTO(task));
        }
        return taskDTOs;
    }
}
