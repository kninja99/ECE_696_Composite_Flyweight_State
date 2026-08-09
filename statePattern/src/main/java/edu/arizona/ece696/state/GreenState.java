package edu.arizona.ece696.state;

/**
 * Concrete {@link TrafficLightState} for the <strong>green</strong> phase: the
 * intersection is open and drivers may proceed. Lasts 4 seconds, then hands off
 * to {@link YellowState}.
 *
 * <p>Stateless and immutable, so a single shared {@link #INSTANCE} is reused
 * everywhere.</p>
 */
public final class GreenState implements TrafficLightState {

    /** The single shared instance of the green phase. */
    public static final GreenState INSTANCE = new GreenState();

    private GreenState() {
        // singleton
    }

    @Override
    public String colorName() {
        return "GREEN";
    }

    @Override
    public int durationSeconds() {
        return 4;
    }

    @Override
    public boolean isIntersectionOpen() {
        return true;
    }

    @Override
    public TrafficLightState next() {
        return YellowState.INSTANCE;
    }
}
