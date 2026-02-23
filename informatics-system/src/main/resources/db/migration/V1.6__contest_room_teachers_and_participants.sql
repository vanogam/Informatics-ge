-- Join table: contest_room <-> teachers (principal)
CREATE TABLE IF NOT EXISTS contest_room_teachers (
    contest_room_id BIGINT NOT NULL,
    teachers_id BIGINT NOT NULL,
    PRIMARY KEY (contest_room_id, teachers_id),
    CONSTRAINT fk_contest_room_teachers_room FOREIGN KEY (contest_room_id)
        REFERENCES contest_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_contest_room_teachers_principal FOREIGN KEY (teachers_id)
        REFERENCES principal(id) ON DELETE CASCADE
);

-- Join table: contest_room <-> participants (principal)
CREATE TABLE IF NOT EXISTS contest_room_participants (
    contest_room_id BIGINT NOT NULL,
    participants_id BIGINT NOT NULL,
    PRIMARY KEY (contest_room_id, participants_id),
    CONSTRAINT fk_contest_room_participants_room FOREIGN KEY (contest_room_id)
        REFERENCES contest_room(id) ON DELETE CASCADE,
    CONSTRAINT fk_contest_room_participants_principal FOREIGN KEY (participants_id)
        REFERENCES principal(id) ON DELETE CASCADE
);
