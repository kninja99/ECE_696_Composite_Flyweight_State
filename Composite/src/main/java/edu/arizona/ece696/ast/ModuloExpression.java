package edu.arizona.ece696.ast;

/**
 * Concrete binary composite for the modulo / remainder operator ({@code %}).
 *
 * <p>The operation uses Java's built-in {@code double} remainder operator, which
 * is the truncated (fmod-style) remainder&mdash;the result takes the sign of the
 * dividend&mdash;not {@link Math#IEEEremainder(double, double)}. A zero divisor
 * follows IEEE-754 semantics and yields {@code NaN} rather than throwing.</p>
 */
public final class ModuloExpression extends BinaryExpression {

    /**
     * Creates a modulo node.
     *
     * @param left  the left operand (dividend)
     * @param right the right operand (divisor)
     */
    public ModuloExpression(Expression left, Expression right) {
        super(left, right);
    }

    /** {@inheritDoc} */
    @Override
    protected String symbol() {
        return "%";
    }

    /** {@inheritDoc} */
    @Override
    protected double apply(double leftValue, double rightValue) {
        return leftValue % rightValue;
    }
}
