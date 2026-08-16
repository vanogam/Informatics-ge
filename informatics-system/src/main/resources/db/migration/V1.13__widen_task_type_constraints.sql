-- TaskType gained COMMUNICATION and CheckerType gained MANAGER (see V1.12), but the original
-- schema pins both columns to the ordinals that existed when it was written. Saving a
-- communication task fails with task_checkertype_check / task_tasktype_check until the
-- constraints are widened.
--
-- Ranges are set from the current enums:
--   TaskType    : BATCH=0, COMMUNICATION=1
--   CheckerType : TOKEN=0, YES_NO=1, LINES=2, DOUBLE_E6=3, DOUBLE_E9=4, CUSTOM=5, MANAGER=6
-- Appending a constant to either enum means widening the matching constraint again.

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.constraint_column_usage
               WHERE table_name = 'task' AND constraint_name = 'task_checkertype_check') THEN
        ALTER TABLE task DROP CONSTRAINT task_checkertype_check;
    END IF;
    ALTER TABLE task ADD CONSTRAINT task_checkertype_check
        CHECK (checkertype >= 0 AND checkertype <= 6);
END $$;

DO $$
BEGIN
    IF EXISTS (SELECT 1 FROM information_schema.constraint_column_usage
               WHERE table_name = 'task' AND constraint_name = 'task_tasktype_check') THEN
        ALTER TABLE task DROP CONSTRAINT task_tasktype_check;
    END IF;
    ALTER TABLE task ADD CONSTRAINT task_tasktype_check
        CHECK (tasktype >= 0 AND tasktype <= 1);
END $$;
