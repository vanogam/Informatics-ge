-- TestStatus gained SYSTEM_ERROR, but the original schema pins the column to the ordinals that
-- existed when it was written. Any worker callback reporting SYSTEM_ERROR fails to persist with
-- submission_submissiontestresults_teststatus_check until the constraint is widened.
--
-- Range is set from the current enum:
--   TestStatus : TIME_LIMIT_EXCEEDED=0, MEMORY_LIMIT_EXCEEDED=1, RUNTIME_ERROR=2, WRONG_ANSWER=3,
--                PARTIAL=4, CORRECT=5, SYSTEM_ERROR=6
-- Appending a constant to the enum means widening this constraint again.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.constraint_column_usage
               WHERE table_name = 'submission_submissiontestresults'
                 AND constraint_name = 'submission_submissiontestresults_teststatus_check') THEN
        ALTER TABLE submission_submissiontestresults DROP CONSTRAINT submission_submissiontestresults_teststatus_check;
    END IF;
    ALTER TABLE submission_submissiontestresults ADD CONSTRAINT submission_submissiontestresults_teststatus_check
        CHECK (teststatus >= 0 AND teststatus <= 6);
END $$;
