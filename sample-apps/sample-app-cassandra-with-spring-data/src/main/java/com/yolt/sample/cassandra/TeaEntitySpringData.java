package com.yolt.sample.cassandra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.cassandra.core.cql.PrimaryKeyType;
import org.springframework.data.cassandra.core.mapping.Column;
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn;
import org.springframework.data.cassandra.core.mapping.Table;

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
@Table("tea")
public class TeaEntitySpringData {

    public static final String USER_ID_COLUMN = "user_id";
    public static final String TEA_ID_COLUMN = "tea_id";
    public static final String AMOUNT_COLUMN = "amount";

    @ToString.Include
    @PrimaryKeyColumn(name = USER_ID_COLUMN, type = PrimaryKeyType.PARTITIONED)
    private UUID userId;

    @ToString.Include
    @Column(TEA_ID_COLUMN)
    private UUID teaId;

    @Column(AMOUNT_COLUMN)
    private BigDecimal amount;

}