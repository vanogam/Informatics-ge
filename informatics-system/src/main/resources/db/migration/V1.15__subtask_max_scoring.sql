-- IOI-style scoring: a task's score is the sum of the contestant's best award on each subtask,
-- taken across every submission they made. That needs the per-subtask breakdown kept beside the
-- total in two places - on the submission that earned it, and on the contestant's standings row,
-- which carries the running maximum.
--
-- The vector is stored as text ("10,20,0,5,0"), one entry per scoring unit: per group for
-- GROUP_MIN, per test for SUM. SubtaskScores.MAX_TRACKED caps how many entries are ever written,
-- so 2000 characters is well clear of the longest vector the application will produce.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'submission' AND column_name = 'subtaskscores') THEN
        ALTER TABLE submission ADD COLUMN subtaskscores VARCHAR(2000);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'task_results' AND column_name = 'subtaskscores') THEN
        ALTER TABLE task_results ADD COLUMN subtaskscores VARCHAR(2000);
    END IF;
END $$;

-- ScoringType is persisted by ordinal and the original schema pins the column to the two values
-- that existed then (migration-backup.sql:137). Saving a contest scored by SUBTASK_MAX fails with
-- contest_scoringtype_check until the range is widened - the same trap V1.13 was written for.
--   ScoringType : BEST_SUBMISSION=0, LAST_SUBMISSION=1, SUBTASK_MAX=2
DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.constraint_column_usage
               WHERE table_name = 'contest' AND constraint_name = 'contest_scoringtype_check') THEN
        ALTER TABLE contest DROP CONSTRAINT contest_scoringtype_check;
    END IF;
    ALTER TABLE contest ADD CONSTRAINT contest_scoringtype_check
        CHECK (scoringtype >= 0 AND scoringtype <= 2);
END $$;
