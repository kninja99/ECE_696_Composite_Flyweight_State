package edu.arizona.ece696.ast.parser;

/**
 * An immutable lexical token: its {@link TokenType category}, the source text it
 * was scanned from, and the zero-based position of that text in the input string
 * (used for error messages).
 *
 * @param type     the lexical category
 * @param text     the exact source lexeme
 * @param position the zero-based start index of {@code text} in the input
 */
public record Token(TokenType type, String text, int position) {

    @Override
    public String toString() {
        return type + "(\"" + text + "\"@" + position + ")";
    }
}
