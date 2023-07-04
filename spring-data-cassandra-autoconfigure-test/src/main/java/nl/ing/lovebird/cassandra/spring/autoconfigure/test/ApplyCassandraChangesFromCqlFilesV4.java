package nl.ing.lovebird.cassandra.spring.autoconfigure.test;

import com.datastax.driver.core.utils.UUIDs;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.modelmutation.UpdateScriptLocator;

import java.io.File;
import java.util.List;

import static com.datastax.oss.driver.api.querybuilder.QueryBuilder.literal;


/**
 * This is duplicate of ApplyCassandraChangesFromCqlFiles as temporary solution during transition to Cassandra v4.
 */
@Slf4j
@RequiredArgsConstructor
public class ApplyCassandraChangesFromCqlFilesV4 implements CassandraModelMutationApplier {

    private static final Object scriptsAppliedLock = new Object();

    private static boolean scriptsApplied = false;
    private static boolean mutationTableCreated = false;

    private final CqlStatementParserV4 cqlStatementParser = new CqlStatementParserV4();
    private final ModelMutationStatementCreatorV4 modelMutationStatementCreatorV4 = new ModelMutationStatementCreatorV4();

    private final CqlSessionBuilder cqlSessionBuilder;
    private final String keyspaceName;

    @Override
    @SuppressWarnings("squid:S1181")
    public void afterPropertiesSet() {
        log.info("Starting application of cassandra model mutations"); //NOSHERIFF runs only in test context

        final List<String> pathsToUpdateScripts = UpdateScriptLocator.getPathsToUpdateScripts();

        synchronized (scriptsAppliedLock) {
            // When keyspace is not initialized yet we have to temporarily nullify keyspace in session builder before creating session
            try (CqlSession cqlSessionWithoutKeyspace = cqlSessionBuilder.withKeyspace((String) null).build()) {
                initializeKeyspace(cqlSessionWithoutKeyspace, keyspaceName);
            }

            try (CqlSession session = cqlSessionBuilder.withKeyspace(keyspaceName).build()) {
                if (!mutationTableCreated) {
                    createModelMutationTable(session);
                    mutationTableCreated = true;
                }

                try {
                    if (!scriptsApplied) {
                        pathsToUpdateScripts.forEach(path -> {
                            applyModelMutation(path, session);
                            writeInModelMutationTable(path, session);
                        });
                        scriptsApplied = true;
                    }
                } catch (Throwable e) {
                    log.error("Failed to apply cassandra model mutations", e);
                    throw e;
                }
            }
        }
        log.info("Finished application of cassandra model mutations"); //NOSHERIFF runs only in test context
    }

    private void initializeKeyspace(CqlSession cqlSession, String keyspaceName) {
        modelMutationStatementCreatorV4.createKeySpace(keyspaceName).forEach(cqlSession::execute);
    }

    private void createModelMutationTable(CqlSession cqlSession) {
        modelMutationStatementCreatorV4.createModelMutationTable().forEach(cqlSession::execute);
    }

    private void applyModelMutation(final String filePath, final CqlSession session) {
        log.info("applyModelMutation: " + filePath);
        try {
            cqlStatementParser.parseStatementsFromFile(filePath).forEach(session::execute);
        } catch (Exception e) {
            log.error("Error while trying to apply modelmutation {}", filePath, e);
            throw e;
        }
    }

    private void writeInModelMutationTable(String filePath, CqlSession session) {
        final String fileName = new File(filePath).getName();
        session.execute(QueryBuilder.insertInto("modelmutation")
                .value("hcpk", literal("HCPK"))
                .value("filename", literal(fileName))
                .value("time", literal(UUIDs.timeBased()))
                .value("forced", literal(false))
                .value("result", literal("OK")).asCql());
    }
}
