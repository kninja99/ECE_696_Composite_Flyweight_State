package edu.arizona.ece696.state;

/**
 * Concrete {@link TrafficLightState} for the <strong>yellow</strong> phase: the
 * light is about to turn red, but the intersection is still open. Lasts 1 second,
 * then hands off to {@link RedState}, completing the cycle.
 *
 * <p>Stateless and immutable, so a single shared {@link #INSTANCE} is reused
 * everywhere.</p>
 */
public final class YellowState implements TrafficLightState {

    /** The single shared instance of the yellow phase. */
    public static final YellowState INSTANCE = new YellowState();

    private YellowState() {
        // singleton
    }

    @Override
    public String colorName() {
        return "YELLOW";
    }

    @Override
    public int durationSeconds() {
        return 1;
    }

    @Override
    public boolean isIntersectionOpen() {
        return true;
    }

    @Override
    public TrafficLightState next() {
        return RedState.INSTANCE;
    }
}
