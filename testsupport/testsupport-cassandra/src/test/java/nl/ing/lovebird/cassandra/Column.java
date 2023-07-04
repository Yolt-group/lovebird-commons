package nl.ing.lovebird.cassandra;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = Column.TABLE)
public class Column {

    public static final String TABLE = "columns";

    public static final String KEYSPACE_NAME_COLUMN = "keyspace_name";
    public static final String TABLE_NAME_COLUMN = "table_name";
    public static final String COLUMN_NAME_COLUMN = "column_name";

    @PartitionKey
    @com.datastax.driver.mapping.annotations.Column(name = KEYSPACE_NAME_COLUMN)
    private String keyspaceName;

    @ClusteringColumn
    @com.datastax.driver.mapping.annotations.Column(name = TABLE_NAME_COLUMN)
    private String tableName;

    @ClusteringColumn(1)
    @com.datastax.driver.mapping.annotations.Column(name = COLUMN_NAME_COLUMN)
    private String columnName;
}
