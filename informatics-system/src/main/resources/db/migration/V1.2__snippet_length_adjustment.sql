alter table testcase
alter column inputsnippet type varchar(3000) using inputsnippet::varchar(3000);

alter table testcase
alter column outputsnippet type varchar(3000) using outputsnippet::varchar(3000);

