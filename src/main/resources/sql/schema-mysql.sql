USE onebot;

CREATE TABLE IF NOT EXISTS group_sync (
    group_id BIGINT NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS whitelist (
    id BIGINT NOT NULL PRIMARY KEY,
    username VARCHAR(16) NOT NULL,
    UNIQUE(id, username)
);