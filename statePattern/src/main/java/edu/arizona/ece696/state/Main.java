package edu.arizona.ece696.state;

/**
 * Console demonstration of the traffic light driven by the State design pattern.
 *
 * <p>It builds a single {@link TrafficLight} and, once per wall-clock second,
 * prints the current color and countdown timer and then advances the light with
 * {@link TrafficLight#tick()}. This makes both the individual one-second ticks
 * (the countdown decreasing) and the state transitions (the color changing when
 * the countdown expires) visible live on the console &mdash; the timer display a
 * waiting driver would watch.</p>
 *
 * <p>The client here touches only the {@link TrafficLight} context; it never
 * mentions {@code RedState}, {@code GreenState}, or {@code YellowState}.</p>
 *
 * <p>Run with no arguments for the default of 3 full cycles, or pass a single
 * integer to run that many complete red &rarr; green &rarr; yellow cycles:</p>
 * <pre>  java edu.arizona.ece696.state.Main 2</pre>
 */
public final class Main {

    /** Default number of full cycles to run when none is given on the command line. */
    private static final int DEFAULT_CYCLES = 3;

    /** Milliseconds per tick: one real second per simulated second. */
    private static final long TICK_MILLIS = 1000L;

    private Main() {
        // no instances
    }

    /**
     * Program entry point.
     *
     * @param args optionally {@code args[0]} = number of full cycles to run;
     *             defaults to {@value #DEFAULT_CYCLES} if absent or unparseable
     * @throws InterruptedException if the pacing sleep is interrupted
     */
    public static void main(String[] args) throws InterruptedException {
        int cycles = parseCycles(args);

        // One full cycle is the sum of every phase's duration (5 + 4 + 1 = 10s),
        // derived from the states themselves so it stays correct if durations change.
        int ticksPerCycle = RedState.INSTANCE.durationSeconds()
                + GreenState.INSTANCE.durationSeconds()
                + YellowState.INSTANCE.durationSeconds();
        int totalTicks = cycles * ticksPerCycle;

        System.out.println("Traffic light: running " + cycles
                + " full cycle(s) (Red -> Green -> Yellow). Ctrl+C to stop early.");
        System.out.println("-".repeat(60));

        TrafficLight light = new TrafficLight();
        for (int i = 0; i < totalTicks; i++) {
            System.out.println(light.render());
            Thread.sleep(TICK_MILLIS);
            light.tick();
        }

        System.out.println("-".repeat(60));
        System.out.println("Done after " + cycles + " cycle(s).");
    }

    /**
     * Parses the requested number of cycles from the command line, falling back to
     * {@link #DEFAULT_CYCLES} when no argument is given or it is not a positive
     * integer.
     */
    private static int parseCycles(String[] args) {
        if (args.length > 0) {
            try {
                int requested = Integer.parseInt(args[0].trim());
                if (requested > 0) {
                    return requested;
                }
                System.out.println("Cycle count must be positive; using default "
                        + DEFAULT_CYCLES + ".");
            } catch (NumberFormatException e) {
                System.out.println("Could not parse \"" + args[0]
                        + "\" as a cycle count; using default " + DEFAULT_CYCLES + ".");
            }
        }
        return DEFAULT_CYCLES;
    }
}
