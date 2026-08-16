-- Support for IOI/CMS style communication tasks: task-supplied grader sources that are
-- linked into every submission, and a manager process that judges them.

-- Number of solution processes the manager drives. Only used by COMMUNICATION tasks.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns
                   WHERE table_name = 'task' AND column_name = 'numprocesses') THEN
        ALTER TABLE task ADD COLUMN numprocesses INTEGER DEFAULT 1;
    END IF;
END $$;

-- Source files a task hands to the judge. The file on disk is what workers read; this
-- table exists so the task editor can list, download and delete without scanning disk.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'task_file') THEN
        CREATE TABLE task_file (
            id                     BIGSERIAL PRIMARY KEY,
            task_id                BIGINT NOT NULL,
            kind                   INTEGER NOT NULL,   -- TaskFileKind ordinal: 0 GRADER, 1 MANAGER
            file_name              VARCHAR(100) NOT NULL,
            file_address           VARCHAR(1000) NOT NULL,
            size_bytes             BIGINT,
            uploaded_at            TIMESTAMP,
            visible_to_contestants BOOLEAN NOT NULL DEFAULT FALSE,
            CONSTRAINT fk_task_file_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE,
            CONSTRAINT uq_task_file_name UNIQUE (task_id, kind, file_name)
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes
                   WHERE tablename = 'task_file' AND indexname = 'idx_task_file_task_id') THEN
        CREATE INDEX idx_task_file_task_id ON task_file(task_id);
    END IF;
END $$;