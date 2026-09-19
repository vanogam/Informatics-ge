package ge.freeuni.informatics.judgeintegration.model;

import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.submission.SubmissionKind;
import ge.freeuni.informatics.common.model.task.CheckerType;
import ge.freeuni.informatics.common.model.task.TaskType;

public record KafkaTask(
        String taskId,
        String contestId,
        String submissionId,
        String submissionName,
        CodeLanguage language,
        long timeLimitMillis,
        int memoryLimitKB,
        String testId,
        String inputName,
        String outputName,
        CheckerType checkerType,
        TaskType taskType,
        int numProcesses,
        Stage stage,
        /**
         * The judging run this message belongs to. Workers echo it back untouched so that a
         * result from a run that has since been superseded can be recognised and dropped.
         */
        Integer judgeToken,
        /**
         * Whether the submission is source code or the contestant's own output files. Workers
         * deployed before this field existed ignore it, and treat every message as source - which
         * is correct, because a core deployed before them never sends anything else.
         */
        SubmissionKind submissionKind
)   {

}
