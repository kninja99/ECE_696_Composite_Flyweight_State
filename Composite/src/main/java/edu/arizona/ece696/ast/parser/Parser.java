package edu.arizona.ece696.ast.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import edu.arizona.ece696.ast.AddExpression;
import edu.arizona.ece696.ast.DivideExpression;
import edu.arizona.ece696.ast.Expression;
import edu.arizona.ece696.ast.FunctionExpression;
import edu.arizona.ece696.ast.MathFunction;
import edu.arizona.ece696.ast.ModuloExpression;
import edu.arizona.ece696.ast.MultiplyExpression;
import edu.arizona.ece696.ast.NegateExpression;
import edu.arizona.ece696.ast.NumberExpression;
import edu.arizona.ece696.ast.PowerExpression;
import edu.arizona.ece696.ast.SubtractExpression;

/**
 * A recursive-descent parser that turns an arithmetic expression string into an
 * {@link Expression} AST built from the Composite-pattern node classes.
 *
 * <p>The grammar (lowest to highest precedence) is:</p>
 * <pre>
 *   expression := term      (('+' | '-') term)*
 *   term       := factor    (('*' | '/' | '%') factor)*
 *   factor     := '-' factor | power
 *   power      := primary    ('^' factor)?        // right-associative
 *   primary    := NUMBER | function | '(' expression ')'
 *   function   := IDENT '(' (expression (',' expression)*)? ')'
 * </pre>
 *
 * <p>A {@code function} is a call to a built-in {@link MathFunction} such as
 * {@code sqrt(x)} or {@code pow(a, b)}; its arguments are full expressions.</p>
 *
 * <p>Consequences of this grammar: {@code *}, {@code /}, {@code %} bind tighter
 * than {@code +}/{@code -}; {@code ^} binds tighter than {@code *} and than unary
 * minus (so {@code -3^2 = -9}); and {@code ^} is right-associative
 * ({@code 2^3^2 = 2^(3^2) = 512}).</p>
 *
 * <p>Typical use:</p>
 * <pre>{@code
 *   Expression ast = new Parser("2 + 3 * 4").parse();
 *   double value = ast.evaluate(); // 14.0
 * }</pre>
 */
public final class Parser {

    /** Logger used to record each constructed AST for spot-checking. */
    private static final Logger LOGGER = Logger.getLogger(Parser.class.getName());

    private final String source;
    private final List<Token> tokens;
    private int position;

    /**
     * Creates a parser by tokenizing {@code input} with a {@link Lexer}.
     *
     * @param input the expression text to parse
     * @throws ParseException if the input cannot be tokenized
     */
    public Parser(String input) {
        this.source = (input == null) ? "" : input;
        this.tokens = new Lexer(input).tokenize();
    }

    /**
     * Convenience factory: parse a string directly into an AST.
     *
     * @param input the expression text
     * @return the root of the resulting AST
     * @throws ParseException if the input is not a well-formed expression
     */
    public static Expression parseExpression(String input) {
        return new Parser(input).parse();
    }

    /**
     * Parses the input and returns the root of the AST.
     *
     * @return the root {@link Expression}
     * @throws ParseException if the input is not a well-formed expression
     */
    public Expression parse() {
        Expression result = expression();
        Token next = peek();
        if (next.type() != TokenType.EOF) {
            throw new ParseException("Unexpected token '" + next.text() + "'", next.position());
        }
        // Spot-check log: record the original input alongside the AST that was
        // built for it. Uses a supplier so the tree string is only rendered when
        // the logger is actually enabled at INFO.
        LOGGER.info(() -> "AST constructed for \"" + source + "\":"
                + System.lineSeparator() + result.toTreeString());
        return result;
    }

    // --- Grammar rules ----------------------------------------------------

    /** expression := term (('+' | '-') term)* */
    private Expression expression() {
        Expression node = term();
        while (true) {
            TokenType t = peek().type();
            if (t == TokenType.PLUS) {
                advance();
                node = new AddExpression(node, term());
            } else if (t == TokenType.MINUS) {
                advance();
                node = new SubtractExpression(node, term());
            } else {
                return node;
            }
        }
    }

    /** term := factor (('*' | '/' | '%') factor)* */
    private Expression term() {
        Expression node = factor();
        while (true) {
            TokenType t = peek().type();
            if (t == TokenType.STAR) {
                advance();
                node = new MultiplyExpression(node, factor());
            } else if (t == TokenType.SLASH) {
                advance();
                node = new DivideExpression(node, factor());
            } else if (t == TokenType.PERCENT) {
                advance();
                node = new ModuloExpression(node, factor());
            } else {
                return node;
            }
        }
    }

    /** factor := '-' factor | power */
    private Expression factor() {
        if (peek().type() == TokenType.MINUS) {
            advance();
            return new NegateExpression(factor());
        }
        return power();
    }

    /** power := primary ('^' factor)?   (right-associative) */
    private Expression power() {
        Expression base = primary();
        if (peek().type() == TokenType.CARET) {
            advance();
            // Recursing into factor() (not power()) makes '^' right-associative
            // and lets the exponent itself carry a unary minus, e.g. 2^-1.
            return new PowerExpression(base, factor());
        }
        return base;
    }

    /** primary := NUMBER | function | '(' expression ')' */
    private Expression primary() {
        Token token = peek();
        switch (token.type()) {
            case NUMBER -> {
                advance();
                return new NumberExpression(parseNumber(token));
            }
            case IDENT -> {
                return functionCall();
            }
            case LPAREN -> {
                advance();
                Expression inner = expression();
                expect(TokenType.RPAREN, ")");
                return inner;
            }
            case EOF -> throw new ParseException("Unexpected end of input", token.position());
            default -> throw new ParseException(
                    "Expected a number, function, or '(' but found '" + token.text() + "'",
                    token.position());
        }
    }

    /** function := IDENT '(' (expression (',' expression)*)? ')' */
    private Expression functionCall() {
        Token name = peek();
        advance();
        expect(TokenType.LPAREN, "(");
        List<Expression> args = new ArrayList<>();
        if (peek().type() != TokenType.RPAREN) {
            args.add(expression());
            while (peek().type() == TokenType.COMMA) {
                advance();
                args.add(expression());
            }
        }
        expect(TokenType.RPAREN, ")");

        MathFunction function = MathFunction.byName(name.text());
        if (function == null) {
            throw new ParseException("Unknown function '" + name.text() + "'", name.position());
        }
        if (args.size() != function.arity()) {
            throw new ParseException("Function '" + function.functionName() + "' expects "
                    + function.arity() + " argument(s) but got " + args.size(), name.position());
        }
        return new FunctionExpression(function, args);
    }

    // --- Helpers ----------------------------------------------------------

    private double parseNumber(Token token) {
        try {
            return Double.parseDouble(token.text());
        } catch (NumberFormatException e) {
            throw new ParseException("Invalid number '" + token.text() + "'", token.position());
        }
    }

    private Token peek() {
        return tokens.get(position);
    }

    private void advance() {
        if (position < tokens.size() - 1) {
            position++;
        }
    }

    private void expect(TokenType type, String display) {
        Token token = peek();
        if (token.type() != type) {
            throw new ParseException(
                    "Expected '" + display + "' but found '" + token.text() + "'", token.position());
        }
        advance();
    }
}
