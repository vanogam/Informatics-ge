-- Admin-triggered JPlag plagiarism checks: a job picks a contest or a single task (and
-- optionally a set of users), runs one JPlag comparison per task/language pair found in scope,
-- and persists the resulting similarity-sorted comparisons for inspection.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'plagiarism_job') THEN
        CREATE TABLE plagiarism_job (
            id                  BIGSERIAL PRIMARY KEY,
            created_by_user_id  BIGINT NOT NULL REFERENCES principal(id),
            contest_id          BIGINT REFERENCES contest(id),
            task_id             BIGINT REFERENCES task(id),
            usernames           TEXT,
            status              INTEGER NOT NULL DEFAULT 0,
            error_message       VARCHAR(1000),
            created_at          TIMESTAMP NOT NULL DEFAULT now(),
            finished_at         TIMESTAMP
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'plagiarism_run') THEN
        CREATE TABLE plagiarism_run (
            id                 BIGSERIAL PRIMARY KEY,
            job_id             BIGINT NOT NULL REFERENCES plagiarism_job(id) ON DELETE CASCADE,
            task_id            BIGINT NOT NULL REFERENCES task(id),
            language           VARCHAR(20) NOT NULL,
            status             INTEGER NOT NULL DEFAULT 0,
            submission_count   INTEGER,
            comparison_count   INTEGER,
            error_message      VARCHAR(1000),
            started_at         TIMESTAMP,
            finished_at        TIMESTAMP
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes
                   WHERE tablename = 'plagiarism_run' AND indexname = 'idx_plagiarism_run_job') THEN
        CREATE INDEX idx_plagiarism_run_job ON plagiarism_run(job_id);
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'plagiarism_comparison') THEN
        CREATE TABLE plagiarism_comparison (
            id               BIGSERIAL PRIMARY KEY,
            run_id           BIGINT NOT NULL REFERENCES plagiarism_run(id) ON DELETE CASCADE,
            submission_a_id  BIGINT NOT NULL REFERENCES submission(id),
            submission_b_id  BIGINT NOT NULL REFERENCES submission(id),
            username_a       VARCHAR(100) NOT NULL,
            username_b       VARCHAR(100) NOT NULL,
            similarity       REAL NOT NULL
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes
                   WHERE tablename = 'plagiarism_comparison' AND indexname = 'idx_plagiarism_comparison_run_similarity') THEN
        CREATE INDEX idx_plagiarism_comparison_run_similarity ON plagiarism_comparison(run_id, similarity DESC);
    END IF;
END $$;
