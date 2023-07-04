CREATE TABLE IF NOT EXISTS foo2 (
user_id uuid,
profile_id text,
PRIMARY KEY(user_id, profile_id)
);