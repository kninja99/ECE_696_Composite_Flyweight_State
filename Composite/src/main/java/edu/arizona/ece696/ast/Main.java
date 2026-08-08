package edu.arizona.ece696.ast;

import edu.arizona.ece696.ast.parser.ParseException;
import edu.arizona.ece696.ast.parser.Parser;

/**
 * Command-line demonstration of the parser and the Composite AST.
 *
 * <p>For each expression it (1) parses the text into an AST, (2) prints the ASCII
 * tree diagram of that AST, (3) prints the fully parenthesized infix
 * reconstruction, and (4) prints the evaluated {@code double} result. This
 * exercises phases 1 (build the AST), 2 (evaluate by traversal), and 4 (evaluate
 * a variety of expressions) of the assignment.</p>
 *
 * <p>Run with no arguments to see a built-in sample set, or pass expressions as
 * command-line arguments to evaluate your own, e.g.:</p>
 * <pre>  java edu.arizona.ece696.ast.Main "2+3*4" "-3^2"</pre>
 */
public final class Main {

    private Main() {
        // no instances
    }

    /** Built-in sample expressions demonstrating precedence and every operator. */
    private static final String[] SAMPLES = {
            "2 + 3 * 4",
            "(2 + 3) * 4",
            "-3 ^ 2",
            "2 ^ 3 ^ 2",
            "2 ^ -1",
            "7 % 3",
            "7 / 2",
            "-(2 + 1) * 2",
            "1 + 2 - 3 + 4",
            "10 - 2 * 3 + 8 / 4",
            "((1 + 2) * (3 + 4)) % 5",
            "sqrt(16) + pow(2, 3)",
            "sqrt(pow(3, 2) + pow(4, 2))",
            "max(3, 7) - min(3, 7)",
            "10 / (5 - 5)"   // divide-by-zero -> IEEE Infinity (no exception)
    };

    /**
     * Program entry point.
     *
     * @param args optional expressions to evaluate; if empty, a sample set is used
     */
    public static void main(String[] args) {
        String[] expressions = (args.length > 0) ? args : SAMPLES;
        for (String source : expressions) {
            report(source);
        }
    }

    /**
     * Parses, prints, and evaluates a single expression, reporting a parse-time
     * error on malformed input without aborting the whole demo. (Divide-by-zero is
     * not an error here: it evaluates to an IEEE {@code Infinity}/{@code NaN}.)
     */
    private static void report(String source) {
        System.out.println("Expression : " + source);
        try {
            Expression ast = Parser.parseExpression(source);
            System.out.println("AST        :");
            System.out.print(indent(ast.toTreeString()));
            System.out.println("Infix form : " + ast.toInfix());
            System.out.println("Value      : " + ast.evaluate());
        } catch (ParseException e) {
            System.out.println("Parse error: " + e.getMessage());
        }
        System.out.println("-".repeat(60));
    }

    /** Indents every line of a block by four spaces for readable nesting. */
    private static String indent(String block) {
        StringBuilder out = new StringBuilder();
        for (String line : block.split("\\R", -1)) {
            if (!line.isEmpty()) {
                out.append("    ").append(line).append(System.lineSeparator());
            }
        }
        return out.toString();
    }
}
