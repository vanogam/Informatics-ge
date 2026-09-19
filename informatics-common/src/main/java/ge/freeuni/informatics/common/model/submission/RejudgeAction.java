package ge.freeuni.informatics.common.model.submission;

/**
 * What an administrator wants re-done to an already judged submission.
 *
 * <p>The three are nested rather than independent: each one ends by doing everything the next one
 * does. Recompiling produces a new binary and then runs it; running produces new test results and
 * then scores them; scoring only reads what is already stored. The nesting is not orchestrated
 * anywhere - it falls out of the judging callbacks, where a completed compilation publishes the
 * test messages and the last completed test finalises the score.
 */
public enum RejudgeAction {
    /**
     * Rebuild the binary from the task's current graders, then run, then score. The only action
     * that helps when the grader, stub or manager changed - or when the submission never compiled.
     */
    RECOMPILE,
    /**
     * Re-run the existing binary against the task's current testcases, then score. Results for
     * testcases that no longer exist are dropped, and newly added ones are run.
     */
    RERUN,
    /**
     * Recompute the score from the stored test results. For a changed scoring parameter, where the
     * tests themselves are still valid.
     */
    RESCORE
}