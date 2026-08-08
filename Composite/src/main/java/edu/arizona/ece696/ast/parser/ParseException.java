package edu.arizona.ece696.ast.parser;

/**
 * Thrown by the {@link Lexer} or {@link Parser} when the input string is not a
 * well-formed arithmetic expression.
 *
 * <p>The {@linkplain #getPosition() position} records the zero-based index in the
 * input where the problem was detected, which callers can use to point at the
 * offending character.</p>
 */
public class ParseException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /** Zero-based index in the input where the error was detected. */
    private final int position;

    /**
     * Creates a parse exception.
     *
     * @param message  a human-readable description of the problem
     * @param position the zero-based index in the input where it was detected
     */
    public ParseException(String message, int position) {
        super(message + " (at position " + position + ")");
        this.position = position;
    }

    /**
     * Returns the zero-based index in the input where the error was detected.
     *
     * @return the error position
     */
    public int getPosition() {
        return position;
    }
}
