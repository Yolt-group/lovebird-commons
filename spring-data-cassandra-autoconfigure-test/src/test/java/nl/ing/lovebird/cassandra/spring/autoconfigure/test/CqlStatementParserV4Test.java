package nl.ing.lovebird.cassandra.spring.autoconfigure.test;

import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import nl.ing.lovebird.cassandra.spring.autoconfigure.test.CqlStatementParserV4;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class CqlStatementParserV4Test {

    private final CqlStatementParserV4 statementParser = new CqlStatementParserV4();

    @Test
    void testExtractionOfStatementWithoutDelimiter() {
        String statement = "SELECT * FROM users";

        assertThat(statementParser.parseStatements(singletonList(statement))).isEmpty();
    }

    @Test
    void testExtractionOfStatementWithDelimiter() {
        String statement = "SELECT * FROM users;";
        List<SimpleStatement> statements = statementParser.parseStatements(singletonList(statement));

        assertContainsOnly(statements, statement);
    }

    @Test
    void testExtractionOfTwoStatementAndReturns() {
        String statement = "SELECT * \n FROM users; \n SELECT * FROM users;";
        List<String> lines = Arrays.asList(statement.split("\n"));

        assertThat(statementParser.parseStatements(lines)).hasSize(2);
    }

    @Test
    void testExtractionWithReturns() {
        String statement = "SELECT * \n\r FROM users; \n\r SELECT * FROM users;";
        List<String> lines = Arrays.asList(statement.split("\n"));

        assertThat(statementParser.parseStatements(lines)).hasSize(2);
    }

    @Test
    void testExtractStatementFromFile() {
        String expected = "SELECT * FROM foo.modelmutation;";

        List<SimpleStatement> statementsFromFile = statementParser.parseStatementsFromFile("./src/test/resources/cassandraUpdates/1-update.cql");

        assertContainsOnly(statementsFromFile, expected);
    }

    private static void assertContainsOnly(List<SimpleStatement> statements, String statement) {
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0).getQuery()).isEqualTo(statement);
    }
}
