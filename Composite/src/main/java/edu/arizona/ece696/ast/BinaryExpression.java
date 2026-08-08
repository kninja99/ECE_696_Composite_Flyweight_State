package edu.arizona.ece696.ast;

import java.util.Objects;

/**
 * The abstract <em>Composite</em> role of the Composite design pattern for
 * binary (two-operand) arithmetic operators.
 *
 * <p>A binary composite holds two child {@link Expression}s&mdash;a left and a
 * right operand&mdash;each of which may itself be a leaf or another composite.
 * This class implements the display operations ({@link #toInfix()} and
 * {@link #toTree(StringBuilder, String, boolean)}) once for all binary operators;
 * concrete subclasses supply only the operator symbol (via {@link #symbol()}) and
 * the numeric rule (via {@link #apply(double, double)}). This keeps each concrete
 * operator class tiny and focused, while the shared traversal logic lives here.</p>
 *
 * @see AddExpression
 * @see SubtractExpression
 * @see MultiplyExpression
 * @see DivideExpression
 * @see ModuloExpression
 * @see PowerExpression
 */
public abstract class BinaryExpression implements Expression {

    /** The left child operand. */
    private final Expression left;

    /** The right child operand. */
    private final Expression right;

    /**
     * Initializes the two child operands.
     *
     * @param left  the left operand (must not be {@code null})
     * @param right the right operand (must not be {@code null})
     */
    protected BinaryExpression(Expression left, Expression right) {
        this.left = Objects.requireNonNull(left, "left");
        this.right = Objects.requireNonNull(right, "right");
    }

    /**
     * Returns the left child operand.
     *
     * @return the left operand
     */
    public final Expression getLeft() {
        return left;
    }

    /**
     * Returns the right child operand.
     *
     * @return the right operand
     */
    public final Expression getRight() {
        return right;
    }

    /**
     * Returns the single-character operator symbol for this operation
     * (for example {@code "+"} or {@code "^"}). Used only for display.
     *
     * @return the operator symbol
     */
    protected abstract String symbol();

    /**
     * Applies this operator's arithmetic rule to two already-evaluated operands.
     *
     * @param leftValue  the value of the left operand
     * @param rightValue the value of the right operand
     * @return the result of the operation
     */
    protected abstract double apply(double leftValue, double rightValue);

    /**
     * {@inheritDoc}
     *
     * <p>Evaluation is a post-order traversal: both children are evaluated first,
     * then this operator's {@link #apply(double, double)} rule combines them.</p>
     */
    @Override
    public final double evaluate() {
        return apply(left.evaluate(), right.evaluate());
    }

    /** {@inheritDoc} */
    @Override
    public final String toInfix() {
        return "(" + left.toInfix() + " " + symbol() + " " + right.toInfix() + ")";
    }

    /** {@inheritDoc} */
    @Override
    public final void toTree(StringBuilder out, String prefix, boolean isTail) {
        out.append(prefix)
           .append(isTail ? "\\-- " : "|-- ")
           .append(symbol())
           .append(System.lineSeparator());
        String childPrefix = prefix + (isTail ? "    " : "|   ");
        // The left child is drawn first and is not the tail; the right child is.
        left.toTree(out, childPrefix, false);
        right.toTree(out, childPrefix, true);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString() {
        return toInfix();
    }
}
