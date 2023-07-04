package nl.ing.lovebird.cassandra.modelmutation;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public abstract class AbstractCqlStatementParser<T> {

    private static final String SEMICOLON = ";";
    private static final String SPACE = " ";

    private static void logStatement(String statement) {
        if (statement.startsWith("INSERT") && statement.contains("VALUES")) {
            String safeStatement = statement.substring(0, statement.indexOf("VALUES"));
            log.info("Create CQL statement: {}", safeStatement);
        } else {
            log.info("Create CQL statement: {}", statement);
        }
    }

    public List<T> parseStatementsFromFile(final String filePath) {
        try {
            final List<String> lines = Files.readAllLines(Paths.get(filePath));
            return parseStatements(lines);
        } catch (IOException e) {
            throw new IllegalArgumentException("Could not extract contents of a file " + filePath);
        }
    }

    public List<T> parseStatements(final List<String> lines) {
        if (lines == null || lines.isEmpty()) {
            return Collections.emptyList();
        }

        final List<T> statements = new ArrayList<>();
        StringBuilder statementStr = new StringBuilder();
        for (String line : lines) {
            statementStr.append(line);
            if (line.trim().endsWith(SEMICOLON)) {
                String statementString = statementStr.toString().trim();
                final T statement = composeStatement(statementString);
                statements.add(statement);
                logStatement(statementString);
                statementStr = new StringBuilder();
            } else {
                statementStr.append(SPACE);
            }
        }
        return statements;
    }

    protected abstract T composeStatement(String statement);
}
