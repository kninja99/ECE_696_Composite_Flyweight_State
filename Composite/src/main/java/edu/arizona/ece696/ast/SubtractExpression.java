package edu.arizona.ece696.ast;

/**
 * Concrete binary composite for subtraction ({@code -}).
 */
public final class SubtractExpression extends BinaryExpression {

    /**
     * Creates a subtraction node.
     *
     * @param left  the left operand
     * @param right the right operand
     */
    public SubtractExpression(Expression left, Expression right) {
        super(left, right);
    }

    /** {@inheritDoc} */
    @Override
    protected String symbol() {
        return "-";
    }

    /** {@inheritDoc} */
    @Override
    protected double apply(double leftValue, double rightValue) {
        return leftValue - rightValue;
    }
}
