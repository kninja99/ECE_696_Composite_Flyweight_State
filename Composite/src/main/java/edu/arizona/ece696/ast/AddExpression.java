package edu.arizona.ece696.ast;

/**
 * Concrete binary composite for addition ({@code +}).
 */
public final class AddExpression extends BinaryExpression {

    /**
     * Creates an addition node.
     *
     * @param left  the left operand
     * @param right the right operand
     */
    public AddExpression(Expression left, Expression right) {
        super(left, right);
    }

    /** {@inheritDoc} */
    @Override
    protected String symbol() {
        return "+";
    }

    /** {@inheritDoc} */
    @Override
    protected double apply(double leftValue, double rightValue) {
        return leftValue + rightValue;
    }
}
