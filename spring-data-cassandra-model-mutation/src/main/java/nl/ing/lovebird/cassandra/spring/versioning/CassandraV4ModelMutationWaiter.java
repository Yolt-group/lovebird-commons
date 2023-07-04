package nl.ing.lovebird.cassandra.spring.versioning;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.metadata.schema.ClusteringOrder;
import com.datastax.oss.driver.api.querybuilder.select.Select;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CqlFileReader;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;
import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.selectFrom;
import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Wait for model mutations scripts to be applied.
 *
 * A pod should not start until all model mutations have been applied,
 * otherwise it may fail to query a table. This class allows the
 * application startup to wait for the mutations scripts to be applied.
 */
@Slf4j
@RequiredArgsConstructor
public class CassandraV4ModelMutationWaiter {

    private final int secondsToWait;
    private final CqlSessionBuilder sessionBuilder;
    private final String cqlDirectory;

    public enum FileExecutionStatus {
        OK, NOT_EXECUTED, SKIP, UNKNOWN;

        static FileExecutionStatus fromString(String string) {
            if (string == null) {
                return UNKNOWN;
            }

            for (FileExecutionStatus value : values()) {
                if (value.name().equals(string)) {
                    return value;
                }
            }
            log.error("File execution status has unknown value {}", string);
            return UNKNOWN;
        }
    }

    public void blockUntilApplied() {

        try (CqlSession session = sessionBuilder.build()) {
            blockUntilApplied(CqlFileReader.cqlFiles(cqlDirectory), session);
        } catch (Exception e) {
            throw new RuntimeException("Error while waiting for CQL files to be applied", e);
        }
    }

    @SneakyThrows
    private void blockUntilApplied(final List<String> cqlFiles, CqlSession cqlSession) {
        log.info("Waiting for data migration to be complete (only waiting, the applying of the migration is done by the Cassandra updates pod)");

        boolean scriptsAreAppliedWithinTimeLimit = CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    SECONDS.sleep(1);
                    if (checkIfScriptsAreApplied(cqlFiles, cqlSession)) {
                        return true;
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }).get(secondsToWait, SECONDS);

        if (!scriptsAreAppliedWithinTimeLimit) {
            throw new TimeoutException(String.format("The cassandra updates were not completed within %d seconds.", secondsToWait));
        }
    }

    private boolean checkIfScriptsAreApplied(final List<String> cqlFiles, final CqlSession session) {
        log.info("Waiting for files:\n{}\nto be applied by the Cassandra updates pod", String.join("\n", cqlFiles));

        final Select select = selectFrom("modelmutation")
                .columns("filename", "result")
                .whereColumn("hcpk").isEqualTo(literal("HCPK"))
                .orderBy("filename", ClusteringOrder.DESC)
                .orderBy("time", ClusteringOrder.DESC);

        List<Row> executedFileInfo = session.execute(select.build()).all();

        for (String cqlFileName : cqlFiles) {
            log.info("Checking migration has been applied from file {}", cqlFileName);

            // This result was orderd by time descending, so it's looking at the last execution status intentionally, to
            // comply with the previous implementation.
            Optional<Row> appliedCqlFile = executedFileInfo.stream()
                    .filter(it -> cqlFileName.equals(it.getString("filename")))
                    .findFirst();

            if (!appliedCqlFile.isPresent()) {
                log.info("Cql file name not found: {}. Applied = false", cqlFileName);
                return false;
            }
            String result = appliedCqlFile.get().getString("result");
            FileExecutionStatus fileExecutionStatus = FileExecutionStatus.fromString(result);
            if (!EnumSet.of(FileExecutionStatus.SKIP, FileExecutionStatus.OK).contains(fileExecutionStatus)) {
                log.info("File {} currently has a status of {}, so it applied=false", cqlFileName, fileExecutionStatus);
                return false;
            }
            log.info("File {} currently has a status of {}, so it applied=true", cqlFileName, fileExecutionStatus);
        }
        log.info("All updates have been applied.");
        return true;
    }
}
