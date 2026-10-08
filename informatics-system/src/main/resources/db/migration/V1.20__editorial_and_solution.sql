-- Per-task editorial (markdown, like the statement) and solution (highlighted source code,
-- per programming language). Both are teacher-authored, hidden behind a visibility checkbox,
-- and - regardless of that checkbox - only ever shown to contestants once the task is in
-- upsolving mode, never during a live contest.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'task' AND column_name = 'editorial_visible') THEN
        ALTER TABLE task ADD COLUMN editorial_visible BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'task' AND column_name = 'solution_visible') THEN
        ALTER TABLE task ADD COLUMN solution_visible BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'task_editorials') THEN
        CREATE TABLE task_editorials (
            task_id    BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
            language   VARCHAR(10) NOT NULL,
            editorial  TEXT,
            PRIMARY KEY (task_id, language)
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'task_solutions') THEN
        CREATE TABLE task_solutions (
            task_id    BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
            language   VARCHAR(50) NOT NULL,
            code       TEXT,
            PRIMARY KEY (task_id, language)
        );
    END IF;
END $$;
