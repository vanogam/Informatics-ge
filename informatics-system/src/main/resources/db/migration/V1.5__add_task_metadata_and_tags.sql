-- Create tag table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name='tag') THEN
        CREATE TABLE tag (
            id BIGSERIAL PRIMARY KEY,
            name VARCHAR(255) NOT NULL UNIQUE
        );
    END IF;
END $$;

-- Create taskmetadata table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name='taskmetadata') THEN
        CREATE TABLE taskmetadata (
            id BIGINT PRIMARY KEY,
            difficultylevel INTEGER,
            fullsolutions INTEGER NOT NULL DEFAULT 0,
            CONSTRAINT fk_taskmetadata_task FOREIGN KEY (id) REFERENCES task(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- Create task_metadata_tags join table
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables 
                   WHERE table_name='task_metadata_tags') THEN
        CREATE TABLE task_metadata_tags (
            task_metadata_id BIGINT NOT NULL,
            tag_id BIGINT NOT NULL,
            PRIMARY KEY (task_metadata_id, tag_id),
            CONSTRAINT fk_task_metadata_tags_metadata FOREIGN KEY (task_metadata_id) 
                REFERENCES taskmetadata(id) ON DELETE CASCADE,
            CONSTRAINT fk_task_metadata_tags_tag FOREIGN KEY (tag_id) 
                REFERENCES tag(id) ON DELETE CASCADE
        );
    END IF;
END $$;

-- Create indexes for better query performance
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_indexes 
                   WHERE tablename='task_metadata_tags' AND indexname='idx_task_metadata_tags_tag_id') THEN
        CREATE INDEX idx_task_metadata_tags_tag_id ON task_metadata_tags(tag_id);
    END IF;
END $$;
