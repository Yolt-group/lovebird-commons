package nl.ing.lovebird.cassandra;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Table(name = User.TABLE_NAME)
public class User {
    public final static String TABLE_NAME = "users";
    public final static String USER_ID_COLUMN = "user_id";
    public final static String NAME_COLUMN = "name";

    @PartitionKey
    @Column(name = USER_ID_COLUMN)
    private UUID userId;

    @Column(name = "name")
    private String name;

}
