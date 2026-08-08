package edu.arizona.ece696.ast;

/**
 * The <em>Component</em> role of the Composite design pattern.
 *
 * <p>An {@code Expression} is a node in an arithmetic Abstract Syntax Tree (AST).
 * Every node&mdash;whether a {@link NumberExpression leaf} holding a literal value
 * or an operator {@link BinaryExpression composite} holding child expressions&mdash;
 * implements this same interface. Client code (for example the evaluator and the
 * printer) can therefore treat individual leaves and whole subtrees uniformly,
 * which is precisely the intent of the Composite pattern.</p>
 *
 * <p>The interface declares three operations:</p>
 * <ul>
 *   <li>{@link #evaluate()} &mdash; compute the numeric value of the subtree
 *       rooted at this node (a recursive tree traversal);</li>
 *   <li>{@link #toInfix()} &mdash; reconstruct a fully parenthesized infix string
 *       so the structure of the tree can be read back unambiguously;</li>
 *   <li>{@link #toTree(StringBuilder, String, boolean)} &mdash; render an ASCII
 *       tree diagram of the subtree for inspection and testing.</li>
 * </ul>
 *
 * @author ECE 696 &mdash; Composite / Flyweight / State assignment
 */
public interface Expression {

    /**
     * Evaluates this expression by traversing the subtree rooted at this node.
     *
     * @return the {@code double} value of the expression
     */
    double evaluate();

    /**
     * Reconstructs a fully parenthesized infix representation of this subtree.
     *
     * <p>Full parenthesization means the returned string is independent of any
     * precedence rules: {@code 2+3*4} is rendered as {@code (2.0 + (3.0 * 4.0))}.
     * This makes the tree's grouping explicit and easy to verify.</p>
     *
     * @return the infix string for this subtree
     */
    String toInfix();

    /**
     * Appends an ASCII tree diagram of this subtree to {@code out}.
     *
     * <p>This is a classic depth-first traversal that draws connector glyphs so
     * the parent/child structure of the Composite is visible. It is used by
     * {@link #toTreeString()} and by the demonstration {@code Main} class.</p>
     *
     * @param out    the buffer to append to (never {@code null})
     * @param prefix the accumulated indentation/connector prefix for this level
     * @param isTail whether this node is the last child of its parent, which
     *               selects the connector glyph that is drawn
     */
    void toTree(StringBuilder out, String prefix, boolean isTail);

    /**
     * Convenience method that renders this subtree as a standalone ASCII tree.
     *
     * @return a multi-line string diagram of the AST rooted at this node
     */
    default String toTreeString() {
        StringBuilder out = new StringBuilder();
        toTree(out, "", true);
        return out.toString();
    }
}
