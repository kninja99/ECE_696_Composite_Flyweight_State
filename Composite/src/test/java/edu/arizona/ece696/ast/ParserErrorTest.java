package edu.arizona.ece696.ast;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import edu.arizona.ece696.ast.parser.ParseException;
import edu.arizona.ece696.ast.parser.Parser;

/**
 * Verifies that malformed input is rejected with a {@link ParseException} rather
 * than silently mis-parsed or crashing with some other exception. Robust error
 * handling is part of the assignment's "best software-engineering practices".
 */
@DisplayName("Parser error handling")
class ParserErrorTest {

    @ParameterizedTest(name = "rejects: \"{0}\"")
    @ValueSource(strings = {
            "",            // empty input
            "   ",         // only whitespace
            "2 +",         // dangling operator
            "* 3",         // leading binary operator
            "(2 + 3",      // unbalanced open paren
            "2 + 3)",      // unbalanced close paren
            "2 2",         // two numbers, no operator
            "2 & 3",       // unknown character
            "()",          // empty parentheses
            "3.1.4",       // malformed number
            "/2"           // leading '/'
    })
    @DisplayName("throws ParseException on malformed input")
    void rejectsMalformedInput(String bad) {
        assertThrows(ParseException.class, () -> Parser.parseExpression(bad));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("ParseException reports a non-negative position")
    void exceptionCarriesPosition() {
        ParseException ex = assertThrows(ParseException.class,
                () -> Parser.parseExpression("2 + )"));
        assertTrue(ex.getPosition() >= 0, "position should be recorded");
    }
}
