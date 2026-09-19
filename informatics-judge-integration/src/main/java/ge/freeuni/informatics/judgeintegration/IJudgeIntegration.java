package ge.freeuni.informatics.judgeintegration;

import ge.freeuni.informatics.common.dto.RejudgeResultDTO;
import ge.freeuni.informatics.common.exception.InformaticsServerException;
import ge.freeuni.informatics.common.model.CodeLanguage;
import ge.freeuni.informatics.common.model.customtest.CustomTestRun;
import ge.freeuni.informatics.common.model.submission.RejudgeAction;
import ge.freeuni.informatics.common.model.submission.Submission;
import ge.freeuni.informatics.common.model.task.Task;

public interface IJudgeIntegration {

    void addSubmission(Task task, Submission submission) throws InformaticsServerException;

    void addCustomTest(Task task, CustomTestRun run, CodeLanguage language) throws InformaticsServerException;

    /**
     * Re-judges an already judged submission from the given point in the chain.
     *
     * <p>Takes an id rather than an entity because it judges each submission in its own
     * transaction, and has to load the lazily fetched test results and testcases inside it.
     *
     * <p>Never throws: a submission that cannot be re-judged - it is still running, it never
     * compiled, its task has no tests - comes back as a refusal carrying the reason, so that
     * re-judging a batch reports on every submission instead of stopping at the first one.
     */
    RejudgeResultDTO rejudge(long submissionId, RejudgeAction action);

}
