CREATE TABLE IF NOT EXISTS tea
(
    user_id     uuid,
    tea_id      uuid,
    amount      decimal,
    PRIMARY KEY (user_id)
);