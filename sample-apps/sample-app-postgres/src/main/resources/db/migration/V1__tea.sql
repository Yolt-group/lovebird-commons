CREATE TABLE IF NOT EXISTS tea
(
    user_id     uuid        PRIMARY KEY,
    tea_id      uuid        NOT NULL,
    amount      decimal     NOT NULL,
    version     bigint      NOT NULL default 0
);
CREATE INDEX idx_tea_user_id ON tea (user_id);
