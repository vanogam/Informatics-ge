alter table testcase
    add column if not exists publictestcase boolean not null default false;
