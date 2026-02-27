-- Create or reuse special task with code "_customTest"
DO $$
DECLARE
    v_task_id    BIGINT;
BEGIN
    SELECT id INTO v_task_id FROM task WHERE code = '_customTest';

    IF v_task_id IS NULL THEN
        INSERT INTO task (
            code,
            title,
            configaddress,
            tasktype,
            taskscoretype,
            taskscoreparameter,
            timelimitmillis,
            memorylimitmb,
            checkertype,
            inputtemplate,
            outputtemplate,
            taskorder
        ) VALUES (
            '_customTest',
            'Custom test',
            NULL,
            0,                   -- TaskType.BATCH (ordinal)
            0,                   -- TaskScoreType.SUM (ordinal)
            '1.0',
            2000,                -- 2s time limit
            256,                 -- 256 MB memory limit
            0,                   -- CheckerType.TOKEN (ordinal)
            'input*.txt',
            'output*.txt',
            1
        )
        RETURNING id INTO v_task_id;
    END IF;
END $$;

-- Table to track custom test runs (one per UI request)
CREATE TABLE IF NOT EXISTS custom_test_run (
    id               BIGSERIAL PRIMARY KEY,
    external_key     VARCHAR(64) NOT NULL UNIQUE,
    user_id          BIGINT NULL,
    task_id          BIGINT NOT NULL,
    language         VARCHAR(32) NOT NULL,
    submission_file  VARCHAR(255) NOT NULL,
    input_file       VARCHAR(255) NOT NULL,
    output_file      VARCHAR(255) NOT NULL,
    created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
    status           VARCHAR(32) NOT NULL,   -- IN_QUEUE, COMPILING, RUNNING, FINISHED, FAILED, COMPILATION_ERROR, SYSTEM_ERROR
    message          TEXT,
    time_millis      INTEGER,
    memory_kb        INTEGER,
    outcome          TEXT,
    CONSTRAINT fk_custom_test_run_task FOREIGN KEY (task_id) REFERENCES task(id) ON DELETE CASCADE
);

