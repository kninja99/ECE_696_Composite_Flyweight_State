package edu.arizona.ece696.ast;

/**
 * Concrete binary composite for exponentiation ({@code ^}).
 *
 * <p>The operator is <em>right-associative</em> in the grammar, so
 * {@code 2^3^2} parses as {@code 2^(3^2) = 512}. Evaluation delegates to
 * {@link Math#pow(double, double)}.</p>
 */
public final class PowerExpression extends BinaryExpression {

    /**
     * Creates an exponentiation node.
     *
     * @param left  the base
     * @param right the exponent
     */
    public PowerExpression(Expression left, Expression right) {
        super(left, right);
    }

    /** {@inheritDoc} */
    @Override
    protected String symbol() {
        return "^";
    }

    /** {@inheritDoc} */
    @Override
    protected double apply(double leftValue, double rightValue) {
        return Math.pow(leftValue, rightValue);
    }
}
