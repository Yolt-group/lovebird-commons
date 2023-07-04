package nl.ing.lovebird.cassandra.spring.autoconfigure.test;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.AbstractCqlStatementParser;

@Slf4j
class CqlStatementParserV4 extends AbstractCqlStatementParser<SimpleStatement> {

    @Override
    protected SimpleStatement composeStatement(String statement) {
        return new SimpleStatementBuilder(statement).build();
    }
}
