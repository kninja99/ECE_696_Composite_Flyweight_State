package edu.arizona.ece696.state;

/**
 * The <strong>State</strong> role of the State design pattern: the common
 * interface implemented by every concrete traffic-light phase
 * ({@link RedState}, {@link GreenState}, {@link YellowState}).
 *
 * <p>Each concrete state encapsulates everything that varies with the current
 * light color: its display name, how long the phase lasts, whether the
 * intersection may be entered during it, and which phase comes next. Because the
 * {@link TrafficLight} context delegates to whichever state it currently holds,
 * changing the light's behavior is a matter of swapping the state object rather
 * than running conditional {@code if}/{@code switch} logic in the context.</p>
 *
 * <p>Concrete states carry no per-light data, so each is implemented as a single
 * shared, immutable instance (a {@code public static final INSTANCE}).</p>
 */
public interface TrafficLightState {

    /**
     * The human-readable color of this phase, e.g. {@code "RED"}.
     *
     * @return the uppercase color name
     */
    String colorName();

    /**
     * How long this phase lasts, in whole seconds. These are the durations fixed
     * by the assignment: red = 5, yellow = 1, green = 4.
     *
     * @return the phase duration in seconds (always positive)
     */
    int durationSeconds();

    /**
     * Whether a driver may enter the intersection during this phase. Red closes
     * the intersection; green and yellow leave it open.
     *
     * @return {@code true} if the intersection is open, {@code false} if closed
     */
    boolean isIntersectionOpen();

    /**
     * The phase that follows this one, realizing the fixed cycle
     * red &rarr; green &rarr; yellow &rarr; red.
     *
     * @return the successor state (never {@code null})
     */
    TrafficLightState next();
}
