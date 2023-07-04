package nl.ing.lovebird.cassandra.modelmutation;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

class AbstractCqlStatementParserTest {

    private final TestCqlStatementParser statementParser = new TestCqlStatementParser();

    private static void assertContainsOnly(List<String> statements, String statement) {
        assertThat(statements).hasSize(1);
        assertThat(statements.get(0)).isEqualTo(statement);
    }

    @Test
    void testExtractionOfStatementWithoutDelimiter() {
        String statement = "SELECT * FROM users";

        assertThat(statementParser.parseStatements(singletonList(statement))).isEmpty();
    }

    @Test
    void testExtractionOfStatementWithDelimiter() {
        String statement = "SELECT * FROM users;";
        List<String> statements = statementParser.parseStatements(singletonList(statement));
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
        String expected = "SELECT * FROM users;";

        List<String> statementsFromFile = statementParser.parseStatementsFromFile("./src/test/resources/cassandraUpdates/simple_statement.cql");

        assertContainsOnly(statementsFromFile, expected);
    }

    private static class TestCqlStatementParser extends AbstractCqlStatementParser<String> {

        @Override
        public String composeStatement(String statement) {
            return statement;
        }
    }
}
