CREATE KEYSPACE IF NOT EXISTS foo
WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};
