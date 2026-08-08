package edu.arizona.ece696.ast;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import edu.arizona.ece696.ast.parser.Parser;

/**
 * Pins down the evaluator's divide-by-zero behavior. The evaluator uses plain
 * IEEE-754 {@code double} arithmetic, so dividing by zero does <em>not</em> throw:
 * a non-zero value over zero is a signed {@code Infinity}, and {@code 0/0} (and
 * {@code x % 0}) is {@code NaN}. These tests document and lock in that contract.
 */
@DisplayName("Divide/modulo by zero (IEEE-754 double semantics)")
class DivideByZeroTest {

    @Test
    @DisplayName("positive / 0 is +Infinity")
    void positiveOverZeroIsPositiveInfinity() {
        double value = Parser.parseExpression("1/0").evaluate();
        assertEquals(Double.POSITIVE_INFINITY, value);
    }

    @Test
    @DisplayName("negative / 0 is -Infinity")
    void negativeOverZeroIsNegativeInfinity() {
        double value = Parser.parseExpression("-1/0").evaluate();
        assertEquals(Double.NEGATIVE_INFINITY, value);
    }

    @Test
    @DisplayName("a computed zero divisor also yields Infinity")
    void computedZeroDivisorIsInfinity() {
        double value = Parser.parseExpression("6/(3-3)").evaluate();
        assertTrue(Double.isInfinite(value), () -> "expected Infinity but got " + value);
    }

    @Test
    @DisplayName("0 / 0 is NaN")
    void zeroOverZeroIsNaN() {
        double value = Parser.parseExpression("0/0").evaluate();
        assertTrue(Double.isNaN(value), () -> "expected NaN but got " + value);
    }

    @Test
    @DisplayName("x % 0 is NaN")
    void moduloByZeroIsNaN() {
        double value = Parser.parseExpression("5%0").evaluate();
        assertTrue(Double.isNaN(value), () -> "expected NaN but got " + value);
    }

    @Test
    @DisplayName("Infinity propagates through a surrounding expression")
    void infinityPropagates() {
        double value = Parser.parseExpression("5 + 4/0").evaluate();
        assertEquals(Double.POSITIVE_INFINITY, value);
    }

    @Test
    @DisplayName("division by a non-zero value is unaffected")
    void nonZeroDivisionStillWorks() {
        assertEquals(3.5, Parser.parseExpression("7/2").evaluate());
    }
}
