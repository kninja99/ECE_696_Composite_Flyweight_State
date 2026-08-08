/**
 * The parsing front end that turns arithmetic expression text into an
 * {@link edu.arizona.ece696.ast.Expression} AST.
 *
 * <p>Two phases:</p>
 * <ol>
 *   <li>{@link edu.arizona.ece696.ast.parser.Lexer} scans the raw string into a
 *       list of {@link edu.arizona.ece696.ast.parser.Token}s
 *       (categorized by {@link edu.arizona.ece696.ast.parser.TokenType}).</li>
 *   <li>{@link edu.arizona.ece696.ast.parser.Parser} performs recursive-descent
 *       parsing over those tokens, encoding operator precedence and associativity,
 *       and constructs the Composite AST.</li>
 * </ol>
 *
 * <p>Ill-formed input raises {@link edu.arizona.ece696.ast.parser.ParseException},
 * which carries the position of the offending character.</p>
 */
package edu.arizona.ece696.ast.parser;
