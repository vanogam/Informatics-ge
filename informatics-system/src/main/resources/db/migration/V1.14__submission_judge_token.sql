-- Identifies a submission's current judging run.
--
-- Re-judging abandons whatever the previous run still had in flight. Those workers keep going and
-- report back, and their callbacks are indistinguishable from the new run's - the topic key is
-- only "<submissionId>:<testKey>". The token is stamped on every message sent to a worker and
-- echoed back on every callback, so a result from a superseded run can be dropped on arrival.
--
-- Existing rows start at 1 rather than null, so the comparison never has to special-case a
-- submission that predates the column. A callback carrying no token at all is a different matter:
-- that comes from a worker deployed before this change, and is accepted on arrival.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'submission' AND column_name = 'judgetoken') THEN
        ALTER TABLE submission ADD COLUMN judgetoken INTEGER NOT NULL DEFAULT 1;
    END IF;
END $$;