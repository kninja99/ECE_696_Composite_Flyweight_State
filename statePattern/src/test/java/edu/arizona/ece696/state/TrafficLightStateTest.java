package edu.arizona.ece696.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the per-state contract of each concrete {@link TrafficLightState}:
 * color name, phase duration, {@code isIntersectionOpen()}, and the
 * {@code next()} transition wiring. These verify the individual states in
 * isolation from the {@link TrafficLight} context.
 */
@DisplayName("Concrete traffic-light states")
class TrafficLightStateTest {

    @Nested
    @DisplayName("phase durations (red=5, green=4, yellow=1)")
    class Durations {

        @Test
        void red() {
            assertEquals(5, RedState.INSTANCE.durationSeconds());
        }

        @Test
        void green() {
            assertEquals(4, GreenState.INSTANCE.durationSeconds());
        }

        @Test
        void yellow() {
            assertEquals(1, YellowState.INSTANCE.durationSeconds());
        }
    }

    @Nested
    @DisplayName("isIntersectionOpen (only red is closed)")
    class IntersectionOpen {

        @Test
        void redIsClosed() {
            assertFalse(RedState.INSTANCE.isIntersectionOpen());
        }

        @Test
        void greenIsOpen() {
            assertTrue(GreenState.INSTANCE.isIntersectionOpen());
        }

        @Test
        void yellowIsOpen() {
            assertTrue(YellowState.INSTANCE.isIntersectionOpen());
        }
    }

    @Nested
    @DisplayName("next() transitions (red -> green -> yellow -> red)")
    class Transitions {

        @Test
        void redGoesToGreen() {
            assertSame(GreenState.INSTANCE, RedState.INSTANCE.next());
        }

        @Test
        void greenGoesToYellow() {
            assertSame(YellowState.INSTANCE, GreenState.INSTANCE.next());
        }

        @Test
        void yellowGoesToRed() {
            assertSame(RedState.INSTANCE, YellowState.INSTANCE.next());
        }
    }

    @Nested
    @DisplayName("color names")
    class ColorNames {

        @Test
        void names() {
            assertEquals("RED", RedState.INSTANCE.colorName());
            assertEquals("GREEN", GreenState.INSTANCE.colorName());
            assertEquals("YELLOW", YellowState.INSTANCE.colorName());
        }
    }
}
