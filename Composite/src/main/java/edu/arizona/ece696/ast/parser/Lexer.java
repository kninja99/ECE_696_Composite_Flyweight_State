package edu.arizona.ece696.ast.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts an arithmetic expression string into a list of {@link Token}s
 * (the scanning / lexical-analysis phase).
 *
 * <p>The lexer recognizes numeric literals (integers and decimals such as
 * {@code 12}, {@code 3.14}, or {@code .5}), the operators
 * {@code + - * / % ^}, and parentheses. Whitespace is skipped. Any other
 * character causes a {@link ParseException}. A terminating {@link TokenType#EOF}
 * token is always appended.</p>
 */
public final class Lexer {

    private final String input;
    private int index;

    /**
     * Creates a lexer over the given input string.
     *
     * @param input the expression text (must not be {@code null})
     */
    public Lexer(String input) {
        this.input = (input == null) ? "" : input;
    }

    /**
     * Scans the entire input and returns the token list, ending with
     * {@link TokenType#EOF}.
     *
     * @return the tokens scanned from the input
     * @throws ParseException if an unexpected character is encountered
     */
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        while (index < input.length()) {
            char c = input.charAt(index);
            if (Character.isWhitespace(c)) {
                index++;
                continue;
            }
            if (isNumberStart(c)) {
                tokens.add(readNumber());
                continue;
            }
            tokens.add(readOperatorOrParen(c));
        }
        tokens.add(new Token(TokenType.EOF, "", index));
        return tokens;
    }

    /** A number may start with a digit or a leading decimal point. */
    private boolean isNumberStart(char c) {
        return Character.isDigit(c) || c == '.';
    }

    /**
     * Reads a numeric literal beginning at the current index. Accepts at most one
     * decimal point.
     */
    private Token readNumber() {
        int start = index;
        boolean seenDot = false;
        while (index < input.length()) {
            char c = input.charAt(index);
            if (Character.isDigit(c)) {
                index++;
            } else if (c == '.' && !seenDot) {
                seenDot = true;
                index++;
            } else {
                break;
            }
        }
        String text = input.substring(start, index);
        if (text.equals(".")) {
            throw new ParseException("Malformed number '.'", start);
        }
        return new Token(TokenType.NUMBER, text, start);
    }

    /** Reads a single-character operator or parenthesis token. */
    private Token readOperatorOrParen(char c) {
        int start = index;
        TokenType type = switch (c) {
            case '+' -> TokenType.PLUS;
            case '-' -> TokenType.MINUS;
            case '*' -> TokenType.STAR;
            case '/' -> TokenType.SLASH;
            case '%' -> TokenType.PERCENT;
            case '^' -> TokenType.CARET;
            case '(' -> TokenType.LPAREN;
            case ')' -> TokenType.RPAREN;
            default -> throw new ParseException("Unexpected character '" + c + "'", start);
        };
        index++;
        return new Token(type, String.valueOf(c), start);
    }
}
