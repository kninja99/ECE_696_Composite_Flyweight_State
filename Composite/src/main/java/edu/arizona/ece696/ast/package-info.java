/**
 * Arithmetic Abstract Syntax Tree (AST) built with the <strong>Composite</strong>
 * design pattern, plus a recursive-descent front end that parses expression text
 * into such a tree.
 *
 * <h2>Composite pattern roles</h2>
 * <ul>
 *   <li><strong>Component</strong> &mdash; {@link edu.arizona.ece696.ast.Expression}:
 *       the common interface for every node. It declares {@code evaluate()} plus the
 *       display operations, letting clients treat leaves and subtrees uniformly.</li>
 *   <li><strong>Leaf</strong> &mdash; {@link edu.arizona.ece696.ast.NumberExpression}:
 *       a terminal node holding a literal {@code double}; the base case of evaluation.</li>
 *   <li><strong>Composite (unary)</strong> &mdash;
 *       {@link edu.arizona.ece696.ast.NegateExpression}: one child, arithmetic negation.</li>
 *   <li><strong>Composite (binary)</strong> &mdash;
 *       {@link edu.arizona.ece696.ast.BinaryExpression} and its concrete subclasses
 *       {@link edu.arizona.ece696.ast.AddExpression Add},
 *       {@link edu.arizona.ece696.ast.SubtractExpression Subtract},
 *       {@link edu.arizona.ece696.ast.MultiplyExpression Multiply},
 *       {@link edu.arizona.ece696.ast.DivideExpression Divide},
 *       {@link edu.arizona.ece696.ast.ModuloExpression Modulo}, and
 *       {@link edu.arizona.ece696.ast.PowerExpression Power}: each holds two child
 *       expressions and combines their values.</li>
 *   <li><strong>Composite (n-ary)</strong> &mdash;
 *       {@link edu.arizona.ece696.ast.FunctionExpression}: a call to a built-in
 *       {@link edu.arizona.ece696.ast.MathFunction} (such as {@code sqrt} or
 *       {@code pow}) holding one child expression per argument.</li>
 * </ul>
 *
 * <h2>Evaluation</h2>
 * Evaluation is a post-order tree traversal: a composite evaluates its children
 * first, then combines the results with its own operator rule. Because both leaves
 * and composites implement {@link edu.arizona.ece696.ast.Expression}, the traversal
 * is fully polymorphic and needs no {@code instanceof} checks.
 *
 * <h2>Front end</h2>
 * The {@link edu.arizona.ece696.ast.parser} subpackage turns an input string into an
 * {@link edu.arizona.ece696.ast.Expression} tree via a {@code Lexer} and a
 * recursive-descent {@code Parser}.
 */
package edu.arizona.ece696.ast;
