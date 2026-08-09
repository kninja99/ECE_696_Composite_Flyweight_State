package edu.arizona.ece696.state;

/**
 * Concrete {@link TrafficLightState} for the <strong>red</strong> phase: the
 * intersection is closed and drivers must stop. Lasts 5 seconds, then hands off
 * to {@link GreenState}.
 *
 * <p>Stateless and immutable, so a single shared {@link #INSTANCE} is reused
 * everywhere.</p>
 */
public final class RedState implements TrafficLightState {

    /** The single shared instance of the red phase. */
    public static final RedState INSTANCE = new RedState();

    private RedState() {
        // singleton
    }

    @Override
    public String colorName() {
        return "RED";
    }

    @Override
    public int durationSeconds() {
        return 5;
    }

    @Override
    public boolean isIntersectionOpen() {
        return false;
    }

    @Override
    public TrafficLightState next() {
        return GreenState.INSTANCE;
    }
}
