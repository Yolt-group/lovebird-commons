package nl.ing.lovebird.cassandrabatch.pager;

import com.datastax.driver.mapping.annotations.ClusteringColumn;
import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Builder
@Table(name = Transaction.TABLE_NAME)
public class Transaction {
    public final static String KEYSPACE = "my_keyspace";
    public final static String TABLE_NAME = "transactions";
    public final static String USER_ID_COLUMN = "user_id";
    public final static String TRANSACTION_ID_COLUMN = "transaction_id";
    public final static String NAME_COLUMN = "name";

    @PartitionKey
    @Column(name = USER_ID_COLUMN)
    private UUID userId;

    @ClusteringColumn
    @Column(name = TRANSACTION_ID_COLUMN)
    private UUID transactionId;

    @Column(name = "name")
    private String name;

}
