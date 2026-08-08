package edu.arizona.ece696.ast;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * The catalog of named mathematical functions the evaluator understands, such as
 * {@code sqrt}, {@code abs}, or {@code pow}.
 *
 * <p>Each constant records its canonical (lower-case) name, its arity (number of
 * arguments), and the numeric rule that computes its result. A static name-index
 * lets the {@link edu.arizona.ece696.ast.parser.Parser} resolve a function by the
 * identifier it scanned. Adding a new function is a one-line change here&mdash;no
 * parser or AST modification is required.</p>
 *
 * <p>Instances are used by {@link FunctionExpression}, the Composite node that
 * represents a function call in the AST.</p>
 */
public enum MathFunction {

    /** Square root, {@code sqrt(x)}. */
    SQRT("sqrt", 1) { @Override double compute(double[] a) { return Math.sqrt(a[0]); } },
    /** Absolute value, {@code abs(x)}. */
    ABS("abs", 1) { @Override double compute(double[] a) { return Math.abs(a[0]); } },
    /** Sine (radians), {@code sin(x)}. */
    SIN("sin", 1) { @Override double compute(double[] a) { return Math.sin(a[0]); } },
    /** Cosine (radians), {@code cos(x)}. */
    COS("cos", 1) { @Override double compute(double[] a) { return Math.cos(a[0]); } },
    /** Tangent (radians), {@code tan(x)}. */
    TAN("tan", 1) { @Override double compute(double[] a) { return Math.tan(a[0]); } },
    /** Exponential, {@code exp(x)} = e^x. */
    EXP("exp", 1) { @Override double compute(double[] a) { return Math.exp(a[0]); } },
    /** Natural logarithm, {@code ln(x)}. */
    LN("ln", 1) { @Override double compute(double[] a) { return Math.log(a[0]); } },
    /** Base-10 logarithm, {@code log10(x)}. */
    LOG10("log10", 1) { @Override double compute(double[] a) { return Math.log10(a[0]); } },

    /** Power, {@code pow(base, exponent)}. */
    POW("pow", 2) { @Override double compute(double[] a) { return Math.pow(a[0], a[1]); } },
    /** Maximum of two values, {@code max(a, b)}. */
    MAX("max", 2) { @Override double compute(double[] a) { return Math.max(a[0], a[1]); } },
    /** Minimum of two values, {@code min(a, b)}. */
    MIN("min", 2) { @Override double compute(double[] a) { return Math.min(a[0], a[1]); } },
    /** Euclidean distance, {@code hypot(a, b)} = sqrt(a^2 + b^2). */
    HYPOT("hypot", 2) { @Override double compute(double[] a) { return Math.hypot(a[0], a[1]); } };

    /** Case-insensitive name index for lookup by the parser. */
    private static final Map<String, MathFunction> BY_NAME = new HashMap<>();

    static {
        for (MathFunction fn : values()) {
            BY_NAME.put(fn.functionName, fn);
        }
    }

    private final String functionName;
    private final int arity;

    MathFunction(String functionName, int arity) {
        this.functionName = functionName;
        this.arity = arity;
    }

    /**
     * Returns the canonical (lower-case) name used in source expressions.
     *
     * @return the function name, e.g. {@code "sqrt"}
     */
    public String functionName() {
        return functionName;
    }

    /**
     * Returns the number of arguments this function requires.
     *
     * @return the arity
     */
    public int arity() {
        return arity;
    }

    /**
     * Computes this function's result. Package-private; callers should use
     * {@link #applyTo(double[])}, which validates the argument count first.
     *
     * @param args the already-evaluated argument values
     * @return the function result
     */
    abstract double compute(double[] args);

    /**
     * Applies this function to the given arguments after checking their count.
     *
     * @param args the evaluated argument values
     * @return the function result
     * @throws IllegalArgumentException if {@code args.length != arity()}
     */
    public double applyTo(double[] args) {
        if (args.length != arity) {
            throw new IllegalArgumentException(
                    functionName + " expects " + arity + " argument(s) but got " + args.length);
        }
        return compute(args);
    }

    /**
     * Looks up a function by name, case-insensitively.
     *
     * @param name the identifier scanned from the source (any case)
     * @return the matching function, or {@code null} if no such function exists
     */
    public static MathFunction byName(String name) {
        if (name == null) {
            return null;
        }
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }
}
