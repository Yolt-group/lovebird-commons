package nl.ing.lovebird.cassandra.spring.autoconfigure.test;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import nl.ing.lovebird.cassandra.modelmutation.AbstractModelMutationStatementCreator;

public class ModelMutationStatementCreatorV4 extends AbstractModelMutationStatementCreator<SimpleStatement> {

    @Override
    protected SimpleStatement composeStatement(String statement) {
        return new SimpleStatementBuilder(statement).build();
    }
}
