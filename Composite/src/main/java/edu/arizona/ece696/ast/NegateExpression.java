package edu.arizona.ece696.ast;

import java.util.Objects;

/**
 * A unary <em>Composite</em>: arithmetic negation (unary minus).
 *
 * <p>Unlike the binary composites, this node holds exactly one child expression.
 * It demonstrates that the Composite pattern accommodates operator nodes of
 * differing arity while still presenting the uniform {@link Expression} interface
 * to clients.</p>
 *
 * <p>Instances are immutable.</p>
 */
public final class NegateExpression implements Expression {

    /** The single operand of the negation. */
    private final Expression operand;

    /**
     * Creates a negation node.
     *
     * @param operand the child expression to negate (must not be {@code null})
     */
    public NegateExpression(Expression operand) {
        this.operand = Objects.requireNonNull(operand, "operand");
    }

    /**
     * Returns the operand being negated.
     *
     * @return the child expression
     */
    public Expression getOperand() {
        return operand;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        return -operand.evaluate();
    }

    /** {@inheritDoc} */
    @Override
    public String toInfix() {
        return "(-" + operand.toInfix() + ")";
    }

    /** {@inheritDoc} */
    @Override
    public void toTree(StringBuilder out, String prefix, boolean isTail) {
        out.append(prefix)
           .append(isTail ? "\\-- " : "|-- ")
           .append("- (unary)")
           .append(System.lineSeparator());
        String childPrefix = prefix + (isTail ? "    " : "|   ");
        operand.toTree(out, childPrefix, true);
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return toInfix();
    }
}
