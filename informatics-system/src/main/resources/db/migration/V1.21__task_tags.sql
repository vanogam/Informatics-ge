-- Staff-assigned tags on a task (e.g. "DP", "Graphs"), filterable and shown below the title on
-- both the task list and the statement page - but, like editorial/solution, only once the task
-- is in upsolving mode, never during a live contest.
--
-- The `tag` table itself already exists, created by V1.5 for an abandoned task-metadata feature
-- (difficulty level / solution count) that never shipped a Java side - nothing in the codebase
-- reads or writes it today, so it's reused here as-is rather than duplicated. This migration
-- only adds the task<->tag join table; V1.5's unrelated `taskmetadata`/`task_metadata_tags`
-- tables are left alone.

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables
                   WHERE table_name = 'task_tags') THEN
        CREATE TABLE task_tags (
            task_id  BIGINT NOT NULL REFERENCES task(id) ON DELETE CASCADE,
            tag_id   BIGINT NOT NULL REFERENCES tag(id) ON DELETE CASCADE,
            PRIMARY KEY (task_id, tag_id)
        );
    END IF;
END $$;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes
                   WHERE tablename = 'task_tags' AND indexname = 'idx_task_tags_tag_id') THEN
        CREATE INDEX idx_task_tags_tag_id ON task_tags(tag_id);
    END IF;
END $$;
