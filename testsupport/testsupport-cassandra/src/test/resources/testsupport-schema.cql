CREATE TABLE IF NOT EXISTS test_keyspace.one_key (
    user_id         uuid,
    number          int,
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS test_keyspace.two_keys (
    user_id         uuid,
    account_id      uuid,
    number          int,
    PRIMARY KEY (user_id)
);

CREATE TABLE IF NOT EXISTS test_keyspace.ignored_table (
    pk              uuid,
    number          int,
    PRIMARY KEY (pk)
);
