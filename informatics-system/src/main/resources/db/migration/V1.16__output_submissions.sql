-- Which submission kinds a task accepts. IOI's "BatchAndOutput" is a task that takes either the
-- contestant's source code or the answer files themselves; a pure output-only task takes only the
-- latter.
--
-- Existing tasks keep today's behaviour: code allowed, outputs not. The defaults are declared on
-- the columns so a row inserted by an older application version - one deployed before the entity
-- carried these fields - still lands on the same values.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'task' AND column_name = 'allowcodesubmission') THEN
        ALTER TABLE task ADD COLUMN allowcodesubmission BOOLEAN DEFAULT TRUE;
        UPDATE task SET allowcodesubmission = TRUE WHERE allowcodesubmission IS NULL;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'task' AND column_name = 'allowoutputsubmission') THEN
        ALTER TABLE task ADD COLUMN allowoutputsubmission BOOLEAN DEFAULT FALSE;
        UPDATE task SET allowoutputsubmission = FALSE WHERE allowoutputsubmission IS NULL;
    END IF;
END $$;
