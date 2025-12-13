DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.columns 
               WHERE table_name='task' AND column_name='order') THEN
        ALTER TABLE task RENAME COLUMN "order" TO taskorder;
    END IF;
END $$;

