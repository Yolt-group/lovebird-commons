package nl.ing.lovebird.cassandra.modelmutation;

import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;

import static java.lang.String.format;

@Slf4j
public abstract class AbstractModelMutationStatementCreator<T> {

    public List<T> createKeySpace(String keyspaceName) {
        return Collections.singletonList(
                composeStatement(format("CREATE KEYSPACE IF NOT EXISTS %s WITH replication = {'class':'SimpleStrategy', 'replication_factor' : 1};", keyspaceName))
        );
    }

    public List<T> createModelMutationTable() {
        return Collections.singletonList(
                composeStatement("CREATE TABLE IF NOT EXISTS modelmutation(hcpk text, filename text, time timeuuid, script text, result text, user text, forced boolean, PRIMARY KEY (hcpk, filename, time));")
        );
    }

    protected abstract T composeStatement(String statement);
}
