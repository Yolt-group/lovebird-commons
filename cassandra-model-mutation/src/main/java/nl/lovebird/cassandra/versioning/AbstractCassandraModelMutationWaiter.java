package nl.lovebird.cassandra.versioning;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeoutException;

import static com.datastax.driver.core.querybuilder.QueryBuilder.desc;
import static com.datastax.driver.core.querybuilder.QueryBuilder.eq;
import static java.util.concurrent.TimeUnit.SECONDS;
import static nl.lovebird.cassandra.versioning.AbstractCassandraModelMutationWaiter.FileExecutionStatus.OK;
import static nl.lovebird.cassandra.versioning.AbstractCassandraModelMutationWaiter.FileExecutionStatus.SKIP;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractCassandraModelMutationWaiter {

    final int secondsToWait;

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

    @SneakyThrows
    void blockUntilApplied(final List<String> cqlFiles, final Optional<String> keyspace, final Session session) {
        log.info("Waiting for data migration to be complete (only waiting, the applying of the migration is done by the Cassandra updates pod)");

        boolean scriptsAreAppliedWithinTimeLimit = CompletableFuture.supplyAsync(() -> {
            while (true) {
                try {
                    SECONDS.sleep(1);
                    if (checkIfScriptsAreApplied(cqlFiles, keyspace, session)) {
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

    private boolean checkIfScriptsAreApplied(final List<String> cqlFiles, final Optional<String> keyspace, final Session session) {
        log.info("Waiting for files:\n{}\nto be applied by the Cassandra updates pod", String.join("\n", cqlFiles));

        final Statement statementSelect = keyspace.map(
                ks -> QueryBuilder.select("filename", "result").from(ks, "modelmutation")
        ).orElse(
                QueryBuilder.select("filename", "result").from("modelmutation")
        ).where(eq("hcpk", "HCPK")).orderBy(desc("filename"), desc("time"));

        final List<Row> executedFileInfo = session.execute(statementSelect).all();

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
            if (!EnumSet.of(SKIP, OK).contains(fileExecutionStatus)) {
                log.info("File {} currently has a status of {}, so it applied=false", cqlFileName, fileExecutionStatus);
                return false;
            }
            log.info("File {} currently has a status of {}, so it applied=true", cqlFileName, fileExecutionStatus);
        }
        log.info("All updates have been applied.");
        return true;
    }
}
