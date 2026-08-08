package edu.arizona.ece696.ast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Loads the CSV test oracle ({@code /oracle_expressions.csv}) from the test classpath.
 *
 * <p>The oracle file pairs an expression string with its expected {@code double}
 * value, one pair per line. Blank lines and lines whose first non-blank character
 * is {@code '#'} are treated as comments and skipped, which lets the oracle file
 * stay self-documenting. Each remaining line is split on its <em>last</em> comma:
 * the expected value never contains a comma, whereas the expression can (for
 * example {@code pow(2,10)}), so splitting from the right keeps the two columns
 * unambiguous.</p>
 *
 * <p>This helper is the single source of truth for both the value oracle used by
 * {@link ExpressionEvaluatorTest} and any other CSV-driven checks.</p>
 */
final class CsvOracle {

    /** Classpath location of the oracle file. */
    static final String RESOURCE = "/oracle_expressions.csv";

    private CsvOracle() {
        // utility class
    }

    /**
     * One row of the oracle: an expression and its expected value.
     *
     * @param expression the arithmetic expression source text
     * @param expected   the expected evaluation result
     */
    record Row(String expression, double expected) {
    }

    /**
     * Reads and parses every non-comment row of the oracle file.
     *
     * @return the oracle rows, in file order
     */
    static List<Row> rows() {
        List<Row> rows = new ArrayList<>();
        try (InputStream in = CsvOracle.class.getResourceAsStream(RESOURCE)) {
            if (in == null) {
                throw new IllegalStateException("Oracle resource not found on classpath: " + RESOURCE);
            }
            try (BufferedReader reader =
                         new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmed = line.strip();
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                        continue;
                    }
                    int comma = line.lastIndexOf(',');
                    if (comma < 0) {
                        throw new IllegalStateException("Malformed oracle line (no comma): " + line);
                    }
                    String expression = line.substring(0, comma);
                    String expectedText = line.substring(comma + 1).strip();
                    rows.add(new Row(expression, Double.parseDouble(expectedText)));
                }
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to read oracle " + RESOURCE, e);
        }
        return rows;
    }
}
