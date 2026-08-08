package edu.arizona.ece696.ast;

/**
 * The <em>Leaf</em> role of the Composite design pattern.
 *
 * <p>A {@code NumberExpression} is a terminal node of the AST: it wraps a single
 * literal {@code double} value and has no children. Evaluating it simply returns
 * that value, which forms the base case of the recursive tree traversal performed
 * by the operator composites.</p>
 *
 * <p>Instances are immutable.</p>
 */
public final class NumberExpression implements Expression {

    /** The literal value carried by this leaf. */
    private final double value;

    /**
     * Creates a leaf node for the given literal value.
     *
     * @param value the numeric value of this leaf
     */
    public NumberExpression(double value) {
        this.value = value;
    }

    /**
     * Returns the wrapped literal value.
     *
     * @return the value of this leaf
     */
    public double getValue() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        return value;
    }

    /** {@inheritDoc} */
    @Override
    public String toInfix() {
        return Double.toString(value);
    }

    /** {@inheritDoc} */
    @Override
    public void toTree(StringBuilder out, String prefix, boolean isTail) {
        out.append(prefix)
           .append(isTail ? "\\-- " : "|-- ")
           .append(Double.toString(value))
           .append(System.lineSeparator());
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return toInfix();
    }
}
