CREATE KEYSPACE IF NOT EXISTS my_keyspace
WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};

CREATE TABLE IF NOT EXISTS my_keyspace.transactions (
    user_id         uuid,
    transaction_id        uuid,
    name            text,
    PRIMARY KEY ((user_id), transaction_id)
) WITH CLUSTERING ORDER BY (transaction_id ASC);
