alter table submission
alter column compilationmessage type varchar(4000) using compilationmessage::varchar(4000);

alter table submission_submissiontestresults
alter column message type varchar(4000) using message::varchar(4000);

alter table submission_submissiontestresults
alter column outcome type varchar(4000) using outcome::varchar(4000);

alter table submission_submissiontestresults
alter column text type varchar(4000) using text::varchar(4000);

alter table submission_submissiontestresults
alter column testkey type varchar(512) using testkey::varchar(512);
