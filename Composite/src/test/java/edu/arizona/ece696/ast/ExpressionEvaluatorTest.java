package edu.arizona.ece696.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import edu.arizona.ece696.ast.parser.Parser;

/**
 * Oracle-based test of the parser + evaluator.
 *
 * <p>The <em>test oracle</em> is the CSV file {@code /oracle_expressions.csv}: each row
 * supplies an expression and its independently known expected value. For every
 * row this test parses the expression into a Composite AST, evaluates the AST by
 * traversal, and asserts the computed value matches the oracle's expected value
 * (within a small floating-point tolerance). Because the oracle lives outside the
 * production code, new cases can be added simply by editing the CSV.</p>
 */
@DisplayName("Evaluator vs. CSV oracle")
class ExpressionEvaluatorTest {

    /** Tolerance for floating-point comparison. */
    private static final double EPSILON = 1e-9;

    /** Supplies one parameterized invocation per oracle row. */
    static Stream<Arguments> oracleRows() {
        return CsvOracle.rows().stream()
                .map(row -> Arguments.of(row.expression(), row.expected()));
    }

    @ParameterizedTest(name = "[{index}] \"{0}\" == {1}")
    @MethodSource("oracleRows")
    @DisplayName("evaluates each oracle expression to its expected value")
    void evaluatesToOracleValue(String expression, double expected) {
        double actual = Parser.parseExpression(expression).evaluate();
        assertEquals(expected, actual, EPSILON,
                () -> "Expression \"" + expression + "\" evaluated to " + actual
                        + " but oracle expected " + expected);
    }

    @DisplayName("sanity: the oracle file is non-empty")
    @org.junit.jupiter.api.Test
    void oracleIsNonEmpty() {
        assertEquals(true, CsvOracle.rows().size() > 0, "oracle should contain rows");
    }
}
