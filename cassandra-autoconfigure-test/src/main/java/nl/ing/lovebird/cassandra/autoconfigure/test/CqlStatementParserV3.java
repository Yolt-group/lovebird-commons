package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.SimpleStatement;
import lombok.extern.slf4j.Slf4j;
import nl.ing.lovebird.cassandra.modelmutation.AbstractCqlStatementParser;

@Slf4j
class CqlStatementParserV3 extends AbstractCqlStatementParser<SimpleStatement> {

    @Override
    protected SimpleStatement composeStatement(String statement) {
        return new SimpleStatement(statement);
    }
}
