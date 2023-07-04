CREATE TABLE IF NOT EXISTS my_keyspace.users (
    user_id         uuid,
    name            text,
    PRIMARY KEY (user_id)

);

CREATE keyspace IF NOT EXISTS other_keyspace WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1} AND DURABLE_WRITES=false;
CREATE TABLE IF NOT EXISTS other_keyspace.users (
    user_id         uuid,
    name            text,
    PRIMARY KEY (user_id)

);
CREATE keyspace IF NOT EXISTS ycs_test WITH REPLICATION = {'class':'SimpleStrategy', 'replication_factor':1} AND DURABLE_WRITES=false;
CREATE TABLE IF NOT EXISTS ycs_test.users (
    user_id         uuid,
    name            text,
    PRIMARY KEY (user_id)

);


