package com.yolt.sample.cassandra;

import com.datastax.driver.mapping.annotations.Column;
import com.datastax.driver.mapping.annotations.PartitionKey;
import com.datastax.driver.mapping.annotations.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * To avoid accidental logging of PII always explicitly annotate the fields
 * to include in the string representation.
 * <p>
 * Note that {@link Data} always includes all fields by default.
 */
@ToString(onlyExplicitlyIncluded = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "tea")
public class TeaEntityCassandraV3 {

    public static final String USER_ID_COLUMN = "user_id";
    public static final String TEA_ID_COLUMN = "tea_id";
    public static final String AMOUNT_COLUMN = "amount";

    @ToString.Include
    @PartitionKey
    @Column(name = USER_ID_COLUMN)
    private UUID userId;

    @ToString.Include
    @Column(name = TEA_ID_COLUMN)
    private UUID teaId;

    @Column(name = AMOUNT_COLUMN)
    private BigDecimal amount;

}