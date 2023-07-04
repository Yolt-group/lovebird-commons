package nl.ing.lovebird.cassandra.autoconfigure.test;

import com.datastax.driver.core.SimpleStatement;
import nl.ing.lovebird.cassandra.modelmutation.AbstractModelMutationStatementCreator;

public class ModelMutationStatementCreatorV3 extends AbstractModelMutationStatementCreator<SimpleStatement> {

    @Override
    protected SimpleStatement composeStatement(String statement) {
        return new SimpleStatement(statement);
    }
}
