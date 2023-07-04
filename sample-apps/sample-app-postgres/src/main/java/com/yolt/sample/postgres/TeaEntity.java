package com.yolt.sample.postgres;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.UUID;

import static com.yolt.sample.postgres.TeaEntity.TABLE_NAME;


/**
 * To avoid accidental logging of PII always explicitly annotate the fields
 * to include in the string representation.
 * <p>
 * Note that {@link lombok.Data} always includes all fields by default.
 */
@Entity
@ToString(onlyExplicitlyIncluded = true)
@Getter
@Setter
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = TABLE_NAME)
public class TeaEntity {

    public static final String TABLE_NAME = "tea";
    private static final String USER_ID_COLUMN = "user_id";
    private static final String TEA_ID_COLUMN = "tea_id";
    private static final String AMOUNT_COLUMN = "amount";

    @Id
    @Column(name = USER_ID_COLUMN)
    @NotNull
    private UUID userId;

    @Column(name = TEA_ID_COLUMN)
    @NotNull
    private UUID teaId;

    @Column(name = AMOUNT_COLUMN)
    @NotNull
    private BigDecimal amount;

    @Version
    @NotNull
    private int version;



}