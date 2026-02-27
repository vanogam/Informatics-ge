alter table submission
alter column compilationmessage type varchar(1000) using compilationmessage::varchar(1000);
