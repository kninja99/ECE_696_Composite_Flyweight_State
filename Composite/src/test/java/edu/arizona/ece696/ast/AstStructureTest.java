package edu.arizona.ece696.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import edu.arizona.ece696.ast.parser.Parser;

/**
 * Verifies that the parser builds the correct Composite AST <em>shape</em>&mdash;
 * not just that it evaluates to the right number. This pins down operator
 * precedence, associativity, and the node types the Composite pattern produces,
 * and it checks the two display operations ({@code toInfix} and {@code toTreeString}).
 */
@DisplayName("AST structure & printing")
class AstStructureTest {

    @Nested
    @DisplayName("node types (Composite roles)")
    class NodeTypes {

        @Test
        @DisplayName("a bare literal is a NumberExpression leaf")
        void literalIsLeaf() {
            Expression ast = Parser.parseExpression("42");
            NumberExpression leaf = assertInstanceOf(NumberExpression.class, ast);
            assertEquals(42.0, leaf.getValue());
        }

        @Test
        @DisplayName("'2+3*4' roots at Add with a Multiply on the right (precedence)")
        void precedenceShapesTheTree() {
            Expression ast = Parser.parseExpression("2+3*4");
            AddExpression add = assertInstanceOf(AddExpression.class, ast);
            assertInstanceOf(NumberExpression.class, add.getLeft());
            assertInstanceOf(MultiplyExpression.class, add.getRight());
        }

        @Test
        @DisplayName("'(2+3)*4' roots at Multiply with an Add on the left (parentheses)")
        void parenthesesOverridePrecedence() {
            Expression ast = Parser.parseExpression("(2+3)*4");
            MultiplyExpression mul = assertInstanceOf(MultiplyExpression.class, ast);
            assertInstanceOf(AddExpression.class, mul.getLeft());
            assertInstanceOf(NumberExpression.class, mul.getRight());
        }

        @Test
        @DisplayName("'2^3^2' is right-associative: Power(2, Power(3, 2))")
        void powerIsRightAssociative() {
            Expression ast = Parser.parseExpression("2^3^2");
            PowerExpression outer = assertInstanceOf(PowerExpression.class, ast);
            assertInstanceOf(NumberExpression.class, outer.getLeft());
            assertInstanceOf(PowerExpression.class, outer.getRight());
        }

        @Test
        @DisplayName("'1-2-3' is left-associative: Subtract(Subtract(1,2),3)")
        void subtractionIsLeftAssociative() {
            Expression ast = Parser.parseExpression("1-2-3");
            SubtractExpression outer = assertInstanceOf(SubtractExpression.class, ast);
            assertInstanceOf(SubtractExpression.class, outer.getLeft());
            assertInstanceOf(NumberExpression.class, outer.getRight());
        }

        @Test
        @DisplayName("'-3^2' is Negate(Power(3,2)) — power binds tighter than unary minus")
        void unaryMinusWrapsPower() {
            Expression ast = Parser.parseExpression("-3^2");
            NegateExpression neg = assertInstanceOf(NegateExpression.class, ast);
            assertInstanceOf(PowerExpression.class, neg.getOperand());
        }
    }

    @Nested
    @DisplayName("infix reconstruction")
    class Infix {

        @Test
        @DisplayName("fully parenthesizes according to tree structure")
        void infixIsFullyParenthesized() {
            assertEquals("(2.0 + (3.0 * 4.0))", Parser.parseExpression("2+3*4").toInfix());
            assertEquals("((2.0 + 3.0) * 4.0)", Parser.parseExpression("(2+3)*4").toInfix());
            assertEquals("(-(3.0 ^ 2.0))", Parser.parseExpression("-3^2").toInfix());
        }
    }

    @Nested
    @DisplayName("ASCII tree printing")
    class TreePrinting {

        @Test
        @DisplayName("prints one line per node with connector glyphs")
        void treeHasExpectedLines() {
            String tree = Parser.parseExpression("2+3*4").toTreeString();
            String[] lines = tree.strip().split("\\R");
            // Root (+) plus four descendants: 2, *, 3, 4 => five nodes total.
            assertEquals(5, lines.length, () -> "unexpected tree:\n" + tree);
            assertTrue(lines[0].contains("+"), "root line should show '+'");
            assertTrue(tree.contains("\\--") || tree.contains("|--"),
                    "tree should contain connector glyphs");
        }
    }

    @Nested
    @DisplayName("Composite uniformity")
    class Uniformity {

        @Test
        @DisplayName("children of a composite are themselves Expressions")
        void childrenAreExpressions() {
            BinaryExpression add = (BinaryExpression) Parser.parseExpression("1+2");
            // Uniform treatment: a child reference is typed as the Component interface.
            Expression child = add.getLeft();
            assertSame(child, add.getLeft());
            assertEquals(1.0, child.evaluate());
        }
    }
}
