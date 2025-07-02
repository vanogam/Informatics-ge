package ge.freeuni.informatics.controller.model;

import ge.freeuni.informatics.common.model.task.CheckerType;
import ge.freeuni.informatics.common.model.task.TaskScoreType;
import ge.freeuni.informatics.common.model.task.TaskType;
import java.util.Map;

public record AddTaskRequest(
        Long taskId,
        Integer contestId,
        String code,
        String title,
        TaskType taskType,
        TaskScoreType taskScoreType,
        /**
         * Describes how to distribute score to test cases.
         * For formatting info, see TaskScoreType class.
         */
        String taskScoreParameter,
        Integer timeLimitMillis,
        Integer memoryLimitMB,
        CheckerType checkerType,
        String inputTemplate,
        String outputTemplate
) {}