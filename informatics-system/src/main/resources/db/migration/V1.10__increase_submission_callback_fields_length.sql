alter table submission
alter column compilationmessage type varchar(2000) using compilationmessage::varchar(2000);

alter table submission_submissiontestresults
alter column message type varchar(2000) using message::varchar(2000);

alter table submission_submissiontestresults
alter column outcome type varchar(2000) using outcome::varchar(2000);

alter table submission_submissiontestresults
alter column text type varchar(2000) using text::varchar(2000);
