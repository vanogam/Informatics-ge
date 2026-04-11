-- Make password_salt nullable since bcrypt hashes include the salt internally.
-- Existing SHA-256 users keep their salt until they log in and get transparently migrated.
ALTER TABLE principal ALTER COLUMN password_salt DROP NOT NULL;

-- Ensure password column can hold bcrypt hashes (60 chars) with room to spare.
ALTER TABLE principal ALTER COLUMN password TYPE varchar(255);
