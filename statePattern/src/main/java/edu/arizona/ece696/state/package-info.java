/**
 * A console traffic light / semaphore implemented with the <strong>State</strong>
 * design pattern.
 *
 * <p>The light cycles red &rarr; green &rarr; yellow &rarr; red, displaying the
 * current color and a countdown timer of the seconds remaining in each phase so a
 * waiting driver knows how long is left. The phase durations are fixed by the
 * assignment: red = 5 s, green = 4 s, yellow = 1 s.</p>
 *
 * <h2>State pattern roles</h2>
 * <ul>
 *   <li><strong>Context</strong> &mdash; {@link edu.arizona.ece696.state.TrafficLight}:
 *       the semaphore the client works with. It holds the current state and the
 *       remaining-time countdown and delegates all color-specific behavior to the
 *       state, so it contains no {@code if}/{@code switch} on the color.</li>
 *   <li><strong>State</strong> &mdash; {@link edu.arizona.ece696.state.TrafficLightState}:
 *       the interface every phase implements, declaring the color name, phase
 *       duration, {@code isIntersectionOpen()}, and the successor phase.</li>
 *   <li><strong>Concrete states</strong> &mdash;
 *       {@link edu.arizona.ece696.state.RedState} (closed, 5 s, &rarr; green),
 *       {@link edu.arizona.ece696.state.GreenState} (open, 4 s, &rarr; yellow), and
 *       {@link edu.arizona.ece696.state.YellowState} (open, 1 s, &rarr; red). Each is
 *       a stateless singleton.</li>
 *   <li><strong>Client</strong> &mdash; {@link edu.arizona.ece696.state.Main}:
 *       drives the light one tick per second and prints the timer; it references
 *       only the context, never the concrete states.</li>
 * </ul>
 *
 * <h2>How a transition happens</h2>
 * {@link edu.arizona.ece696.state.TrafficLight#tick()} decrements the countdown;
 * when it reaches zero the context asks the current state for its successor
 * ({@link edu.arizona.ece696.state.TrafficLightState#next()}) and resets the
 * countdown to the new phase's duration. Changing the light's behavior is thus a
 * matter of swapping the state object, not editing conditional logic.
 */
package edu.arizona.ece696.state;
