alter table task_statements
alter column statement type varchar(100000) using statement::varchar(100000);
