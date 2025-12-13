DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name='task' AND column_name='order') THEN
        ALTER TABLE task ADD COLUMN "order" INTEGER NOT NULL DEFAULT 0;
    END IF;
END $$;

