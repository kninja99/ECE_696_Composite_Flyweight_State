package edu.arizona.ece696.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * Behavioral tests for the {@link TrafficLight} context: its initial state, the
 * per-second countdown, the state transitions that happen when a phase expires,
 * delegation of {@code isIntersectionOpen()}, and the rendered console output.
 */
@DisplayName("TrafficLight context behavior")
class TrafficLightTest {

    /** Advances the light {@code n} logical seconds. */
    private static void tick(TrafficLight light, int n) {
        for (int i = 0; i < n; i++) {
            light.tick();
        }
    }

    @Nested
    @DisplayName("initial state")
    class Initial {

        @Test
        void startsOnRedWithFullTimer() {
            TrafficLight light = new TrafficLight();
            assertSame(RedState.INSTANCE, light.getState());
            assertEquals("RED", light.getColorName());
            assertEquals(5, light.getRemainingSeconds());
            assertFalse(light.isIntersectionOpen());
        }

        @Test
        void canStartOnAGivenState() {
            TrafficLight light = new TrafficLight(GreenState.INSTANCE);
            assertSame(GreenState.INSTANCE, light.getState());
            assertEquals(4, light.getRemainingSeconds());
        }
    }

    @Nested
    @DisplayName("countdown within a phase")
    class Countdown {

        @Test
        void decrementsEachTickWithoutChangingColor() {
            TrafficLight light = new TrafficLight(); // RED, 5
            light.tick();
            assertSame(RedState.INSTANCE, light.getState());
            assertEquals(4, light.getRemainingSeconds());
            light.tick();
            assertSame(RedState.INSTANCE, light.getState());
            assertEquals(3, light.getRemainingSeconds());
        }
    }

    @Nested
    @DisplayName("transition on expiry")
    class Expiry {

        @Test
        void redBecomesGreenAfterFiveTicks() {
            TrafficLight light = new TrafficLight(); // RED, 5
            tick(light, 5);
            assertSame(GreenState.INSTANCE, light.getState());
            assertEquals(4, light.getRemainingSeconds());
            assertTrue(light.isIntersectionOpen());
        }

        @Test
        void greenBecomesYellowAfterFourMoreTicks() {
            TrafficLight light = new TrafficLight();
            tick(light, 5 + 4); // through red, through green
            assertSame(YellowState.INSTANCE, light.getState());
            assertEquals(1, light.getRemainingSeconds());
            assertTrue(light.isIntersectionOpen());
        }

        @Test
        void yellowBecomesRedAfterOneMoreTick() {
            TrafficLight light = new TrafficLight();
            tick(light, 5 + 4 + 1); // a full cycle
            assertSame(RedState.INSTANCE, light.getState());
            assertEquals(5, light.getRemainingSeconds());
            assertFalse(light.isIntersectionOpen());
        }

        @Test
        void fullCycleReturnsToStart() {
            TrafficLight light = new TrafficLight();
            int ticksPerCycle = 5 + 4 + 1;
            // Two full cycles should land back on red with a full timer.
            tick(light, ticksPerCycle * 2);
            assertSame(RedState.INSTANCE, light.getState());
            assertEquals(5, light.getRemainingSeconds());
        }
    }

    @Nested
    @DisplayName("render() output")
    class Render {

        @Test
        void redOutputShowsColorTimerAndClosed() {
            TrafficLight light = new TrafficLight(); // RED, 5
            String line = light.render();
            assertTrue(line.contains("RED"), line);
            assertTrue(line.contains("5"), line);
            assertTrue(line.contains("CLOSED"), line);
            assertFalse(line.contains("OPEN"), line);
        }

        @Test
        void greenOutputShowsColorTimerAndOpen() {
            TrafficLight light = new TrafficLight(GreenState.INSTANCE); // GREEN, 4
            String line = light.render();
            assertTrue(line.contains("GREEN"), line);
            assertTrue(line.contains("4"), line);
            assertTrue(line.contains("OPEN"), line);
        }

        @Test
        void yellowOutputShowsColorTimerAndOpen() {
            TrafficLight light = new TrafficLight(YellowState.INSTANCE); // YELLOW, 1
            String line = light.render();
            assertTrue(line.contains("YELLOW"), line);
            assertTrue(line.contains("1"), line);
            assertTrue(line.contains("OPEN"), line);
        }
    }
}
