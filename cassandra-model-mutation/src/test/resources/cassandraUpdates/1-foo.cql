CREATE TABLE IF NOT EXISTS foo.foo (
partition_key uuid,
clustering_column text,
PRIMARY KEY(partition_key, clustering_column)
);