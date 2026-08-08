package edu.arizona.ece696.ast;

/**
 * Concrete binary composite for multiplication ({@code *}).
 */
public final class MultiplyExpression extends BinaryExpression {

    /**
     * Creates a multiplication node.
     *
     * @param left  the left operand
     * @param right the right operand
     */
    public MultiplyExpression(Expression left, Expression right) {
        super(left, right);
    }

    /** {@inheritDoc} */
    @Override
    protected String symbol() {
        return "*";
    }

    /** {@inheritDoc} */
    @Override
    protected double apply(double leftValue, double rightValue) {
        return leftValue * rightValue;
    }
}
