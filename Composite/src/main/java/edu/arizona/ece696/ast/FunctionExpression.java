package edu.arizona.ece696.ast;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * An n-ary <em>Composite</em>: a call to a named {@link MathFunction} such as
 * {@code sqrt(x)} or {@code pow(a, b)}.
 *
 * <p>This node holds one child {@link Expression} per argument, generalizing the
 * unary and binary composites to any arity. Because its children are ordinary
 * {@code Expression}s, function arguments may be arbitrarily complex subtrees
 * (for example {@code sqrt(pow(3,2) + pow(4,2))}), and the same uniform traversal
 * evaluates and prints them.</p>
 *
 * <p>Instances are immutable.</p>
 */
public final class FunctionExpression implements Expression {

    private final MathFunction function;
    private final List<Expression> arguments;

    /**
     * Creates a function-call node.
     *
     * @param function  the function being called (must not be {@code null})
     * @param arguments the argument expressions; a defensive copy is taken
     * @throws IllegalArgumentException if the number of arguments does not match
     *                                  the function's {@link MathFunction#arity() arity}
     */
    public FunctionExpression(MathFunction function, List<Expression> arguments) {
        this.function = Objects.requireNonNull(function, "function");
        this.arguments = List.copyOf(arguments);
        if (this.arguments.size() != function.arity()) {
            throw new IllegalArgumentException(
                    function.functionName() + " expects " + function.arity()
                            + " argument(s) but got " + this.arguments.size());
        }
    }

    /**
     * Returns the function being called.
     *
     * @return the {@link MathFunction}
     */
    public MathFunction getFunction() {
        return function;
    }

    /**
     * Returns the (immutable) list of argument expressions.
     *
     * @return the arguments
     */
    public List<Expression> getArguments() {
        return arguments;
    }

    /** {@inheritDoc} */
    @Override
    public double evaluate() {
        double[] values = new double[arguments.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = arguments.get(i).evaluate();
        }
        return function.applyTo(values);
    }

    /** {@inheritDoc} */
    @Override
    public String toInfix() {
        String args = arguments.stream()
                .map(Expression::toInfix)
                .collect(Collectors.joining(", "));
        return function.functionName() + "(" + args + ")";
    }

    /** {@inheritDoc} */
    @Override
    public void toTree(StringBuilder out, String prefix, boolean isTail) {
        out.append(prefix)
           .append(isTail ? "\\-- " : "|-- ")
           .append(function.functionName())
           .append("()")
           .append(System.lineSeparator());
        String childPrefix = prefix + (isTail ? "    " : "|   ");
        for (int i = 0; i < arguments.size(); i++) {
            boolean last = (i == arguments.size() - 1);
            arguments.get(i).toTree(out, childPrefix, last);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return toInfix();
    }
}
