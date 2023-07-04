CREATE TABLE IF NOT EXISTS foo (
partition_key uuid,
clustering_column text,
PRIMARY KEY(partition_key, clustering_column)
);