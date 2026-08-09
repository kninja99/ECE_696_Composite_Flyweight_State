package edu.arizona.ece696.state;

/**
 * The <strong>Context</strong> role of the State design pattern: the traffic
 * light / semaphore that clients interact with. It holds a reference to the
 * current {@link TrafficLightState} and a countdown of the seconds remaining in
 * that phase, and delegates all color-specific behavior to that state.
 *
 * <p>Clients (such as {@link Main}) work exclusively through this class &mdash;
 * they call {@link #tick()} to advance time and the query methods to read the
 * light &mdash; and never reference the concrete {@code RedState},
 * {@code GreenState}, or {@code YellowState} classes directly. That is the whole
 * point of the pattern: the transition rules live inside the states, so the
 * context has no {@code if}/{@code switch} on the current color.</p>
 *
 * <p>Timing is deliberately kept out of this class: {@link #tick()} advances the
 * simulation by exactly one second of logical time and does no sleeping or
 * printing, which makes the state machine fully unit-testable. The real-time
 * pacing (one tick per wall-clock second) lives in {@link Main}.</p>
 */
public final class TrafficLight {

    private final TrafficLightState startState;
    private TrafficLightState state;
    private int remainingSeconds;
    private int completedCycles;

    /**
     * Creates a traffic light that starts in the red phase with its full duration
     * remaining.
     */
    public TrafficLight() {
        this(RedState.INSTANCE);
    }

    /**
     * Creates a traffic light that starts in the given phase with that phase's
     * full duration remaining. Useful for tests that want to begin mid-cycle.
     *
     * @param initialState the phase to start in (must not be {@code null})
     */
    public TrafficLight(TrafficLightState initialState) {
        if (initialState == null) {
            throw new IllegalArgumentException("initialState must not be null");
        }
        this.startState = initialState;
        this.state = initialState;
        this.remainingSeconds = initialState.durationSeconds();
        this.completedCycles = 0;
    }

    /**
     * Advances the light by one second of logical time. The countdown for the
     * current phase decreases by one; when it reaches zero the light transitions
     * to the next phase (as decided by the current state) and the countdown is
     * reset to the new phase's duration. Returning to the phase the light started
     * in counts as one completed cycle.
     */
    public void tick() {
        remainingSeconds--;
        if (remainingSeconds <= 0) {
            state = state.next();
            remainingSeconds = state.durationSeconds();
            if (state == startState) {
                completedCycles++;
            }
        }
    }

    /**
     * The color of the current phase, e.g. {@code "RED"}.
     *
     * @return the current color name
     */
    public String getColorName() {
        return state.colorName();
    }

    /**
     * The number of seconds left before the light changes to the next phase.
     *
     * @return the seconds remaining in the current phase
     */
    public int getRemainingSeconds() {
        return remainingSeconds;
    }

    /**
     * Whether the intersection is currently open, delegated to the current state.
     *
     * @return {@code true} if drivers may enter the intersection now
     */
    public boolean isIntersectionOpen() {
        return state.isIntersectionOpen();
    }

    /**
     * The number of full cycles the light has completed &mdash; that is, how many
     * times it has transitioned back to the phase it started in. Lets a client run
     * "N full cycles" without knowing anything about the individual phases or their
     * durations.
     *
     * @return the count of completed cycles
     */
    public int getCompletedCycles() {
        return completedCycles;
    }

    /**
     * The current state object. Exposed mainly so tests can assert on the exact
     * phase; ordinary clients should prefer the color/timer query methods.
     *
     * @return the current {@link TrafficLightState}
     */
    public TrafficLightState getState() {
        return state;
    }

    /**
     * Renders the current light as a single console line showing the color, a
     * countdown bar and number for the remaining time, and whether the
     * intersection is open. Returned as a {@code String} (rather than printed
     * directly) so the exact output can be both displayed by {@link Main} and
     * asserted by tests.
     *
     * <p>Example: {@code RED    | time remaining: [#####] 5s | intersection: CLOSED}</p>
     *
     * @return the formatted display line for the current phase
     */
    public String render() {
        String bar = "#".repeat(Math.max(0, remainingSeconds));
        return String.format("%-6s | time remaining: [%s] %ds | intersection: %s",
                state.colorName(), bar, remainingSeconds,
                state.isIntersectionOpen() ? "OPEN" : "CLOSED");
    }
}
