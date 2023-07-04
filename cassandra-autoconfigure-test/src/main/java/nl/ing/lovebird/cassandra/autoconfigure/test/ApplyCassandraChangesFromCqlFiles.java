package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import com.datastax.driver.core.utils.UUIDs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.CassandraModelMutationApplier;
import nl.ing.lovebird.cassandra.modelmutation.UpdateScriptLocator;

import java.io.File;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
class ApplyCassandraChangesFromCqlFiles implements CassandraModelMutationApplier {

    private static final Object scriptsAppliedLock = new Object();
    private static boolean scriptsApplied = false;
    private static boolean mutationTableCreated = false;

    private final CqlStatementParserV3 cqlStatementParser = new CqlStatementParserV3();
    private final ModelMutationStatementCreatorV3 modelMutationStatementCreator = new ModelMutationStatementCreatorV3();

    private final Cluster cluster;
    private final String keyspaceName;

    @Override
    public void afterPropertiesSet() {
        final List<String> pathsToUpdateScripts = UpdateScriptLocator.getPathsToUpdateScripts();

        synchronized (scriptsAppliedLock) {
            try (Session session = cluster.connect()) {
                initializeKeyspace(session, keyspaceName);
            }

            try (Session session = cluster.connect(keyspaceName)) {
                if (!mutationTableCreated) {
                    createModelMutationTable(session);
                    mutationTableCreated = true;
                }

                if (!scriptsApplied) {
                    pathsToUpdateScripts.forEach(path -> {
                        applyModelMutation(path, session);
                        writeInModelMutationTable(path, session);
                    });
                    scriptsApplied = true;
                }
            }
        }
    }

    private void initializeKeyspace(Session session, String keyspaceName) {
        modelMutationStatementCreator.createKeySpace(keyspaceName).forEach(session::execute);
    }

    private void createModelMutationTable(Session session) {
        modelMutationStatementCreator.createModelMutationTable().forEach(session::execute);
    }

    private void applyModelMutation(final String filePath, final Session session) {
        log.info("applyModelMutation: " + filePath);
        try {
            cqlStatementParser.parseStatementsFromFile(filePath).forEach(session::execute);
        } catch (Exception e) {
            log.error("Error while trying to apply modelmutation {}", filePath, e);
            throw e;
        }
    }

    private void writeInModelMutationTable(String filePath, Session session) {
        final String fileName = new File(filePath).getName();
        session.execute(QueryBuilder.insertInto("modelmutation")
                .value("hcpk", "HCPK")
                .value("filename", fileName)
                .value("time", UUIDs.timeBased())
                .value("forced", false)
                .value("result", "OK"));
    }
}
