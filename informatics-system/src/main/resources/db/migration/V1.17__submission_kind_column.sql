-- SubmissionKind (SOURCE/OUTPUT) for the Submission entity, persisted by enum ordinal like the
-- other enums here. Missed when V1.16 added the task-level allow*Submission columns; existing
-- rows are all source submissions, which is also the entity's in-memory default (ordinal 0).
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'submission' AND column_name = 'submissionkind') THEN
        ALTER TABLE submission ADD COLUMN submissionkind INTEGER DEFAULT 0;
        UPDATE submission SET submissionkind = 0 WHERE submissionkind IS NULL;
    END IF;
END $$;
