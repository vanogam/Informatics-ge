alter table testcase
alter column inputfileaddress type varchar(512) using inputfileaddress::varchar(512);

alter table testcase
alter column outputfileaddress type varchar(512) using outputfileaddress::varchar(512);

alter table testcase
alter column inputsnippet type varchar(3000) using inputsnippet::varchar(3000);

alter table testcase
alter column outputsnippet type varchar(3000) using outputsnippet::varchar(3000);
