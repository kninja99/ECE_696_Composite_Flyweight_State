package edu.arizona.ece696.ast;

/**
 * Concrete binary composite for division ({@code /}).
 *
 * <p>Division follows IEEE-754 {@code double} semantics: dividing a non-zero value
 * by zero yields a signed {@code Infinity}, and {@code 0/0} yields {@code NaN},
 * rather than throwing an exception. This behavior is intentional and is exercised
 * by {@code DivideByZeroTest}.</p>
 */
public final class DivideExpression extends BinaryExpression {

    /**
     * Creates a division node.
     *
     * @param left  the left operand (dividend)
     * @param right the right operand (divisor)
     */
    public DivideExpression(Expression left, Expression right) {
        super(left, right);
    }

    /** {@inheritDoc} */
    @Override
    protected String symbol() {
        return "/";
    }

    /** {@inheritDoc} */
    @Override
    protected double apply(double leftValue, double rightValue) {
        return leftValue / rightValue;
    }
}
