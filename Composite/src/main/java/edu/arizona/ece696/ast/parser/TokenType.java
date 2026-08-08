package edu.arizona.ece696.ast.parser;

/**
 * The lexical categories produced by the {@link Lexer} and consumed by the
 * {@link Parser}.
 */
public enum TokenType {
    /** A numeric literal, e.g. {@code 42} or {@code 3.14}. */
    NUMBER,
    /** The {@code +} operator. */
    PLUS,
    /** The {@code -} operator (binary subtraction or unary minus). */
    MINUS,
    /** The {@code *} operator. */
    STAR,
    /** The {@code /} operator. */
    SLASH,
    /** The {@code %} operator. */
    PERCENT,
    /** The {@code ^} operator. */
    CARET,
    /** A left parenthesis {@code (}. */
    LPAREN,
    /** A right parenthesis {@code )}. */
    RPAREN,
    /** A sentinel marking the end of the token stream. */
    EOF
}
