# Traffic Light — State Design Pattern

An ECE 696 design-patterns assignment. A console **traffic light / semaphore**
whose behavior is driven entirely by the **State design pattern**. The light
cycles Red → Green → Yellow → Red, showing the current color and a **countdown
timer** of the seconds remaining in the current phase, so a waiting driver knows
how long is left. Each phase answers `boolean isIntersectionOpen()`, and the
**client works only with the `TrafficLight` context — never with the concrete
state classes**.

## What it does

1. **Model each phase as a state.** `RedState`, `GreenState`, and `YellowState`
   each know their color, duration, whether the intersection is open, and which
   phase comes next.
2. **Delegate from the context.** `TrafficLight` (the semaphore) holds the current
   state plus a remaining-time countdown and delegates all color-specific behavior
   to the state — no `if`/`switch` on the color.
3. **Tick the timer.** `TrafficLight.tick()` counts the phase down one second at a
   time; when it hits zero the light transitions to the next phase and resets the
   countdown.
4. **Display it live.** `Main` prints the color and countdown once per second, so
   both individual ticks and state transitions are visible on the console.

## Phase rules

| Phase  | Duration | `isIntersectionOpen()` | Next phase |
|--------|----------|------------------------|------------|
| Red    | 5 s      | `false` (closed)       | Green      |
| Green  | 4 s      | `true` (open)          | Yellow     |
| Yellow | 1 s      | `true` (open)          | Red        |

Only **red** closes the intersection; green and yellow leave it open.

## State pattern roles

| Role                       | Class(es) |
|----------------------------|-----------|
| Context ("semaphore")      | `TrafficLight` |
| State (interface)          | `TrafficLightState` |
| Concrete states            | `RedState`, `GreenState`, `YellowState` |
| Client / demo              | `Main` |

The concrete states are stateless singletons (`INSTANCE`), since a phase carries
no per-light data. `TrafficLight.tick()` is kept pure (no sleeping, no printing)
so the state machine is fully unit-testable; the one-tick-per-second pacing lives
in `Main`.

## Project layout

```
statePattern/
├── pom.xml
├── src/main/java/edu/arizona/ece696/state/    # states + context + Main demo
└── src/test/java/edu/arizona/ece696/state/     # JUnit 5 tests
```

## Build & run

### Eclipse (no command-line Maven needed)

1. **File ▸ Import… ▸ Maven ▸ Existing Maven Projects**, select the `statePattern`
   folder, Finish. Eclipse's bundled Maven (m2e) resolves JUnit on first build
   (needs internet once).
2. Run the demo: right-click `Main.java` ▸ **Run As ▸ Java Application**.
3. Run the tests: right-click the `src/test/java` folder ▸ **Run As ▸ JUnit Test**.

### Command line (if Maven is installed)

```bash
mvn test                                   # run all JUnit tests
mvn compile exec:java                      # run the demo (default 3 cycles)
mvn compile exec:java -Dexec.arguments="1" # run a single Red->Green->Yellow cycle
```

### Quick smoke test with only a JDK (no Maven)

From the `statePattern` folder:

```bash
find src/main/java -name "*.java" > sources.txt
javac -d out @sources.txt
java -cp out edu.arizona.ece696.state.Main
```

## Testing

JUnit 5 tests verify every requirement:

* **`TrafficLightStateTest`** — each state's duration (5/4/1), `isIntersectionOpen()`
  (false/true/true), color name, and all three `next()` transitions.
* **`TrafficLightTest`** — the context starts on red with a full timer, the
  countdown decrements each tick, each phase transitions to the next when it
  expires (a full Red→Green→Yellow→Red cycle), `isIntersectionOpen()` delegates
  correctly, and `render()` produces the expected color / timer / open-closed text.

## Sample output

```
Traffic light: running 3 full cycle(s) (Red -> Green -> Yellow). Ctrl+C to stop early.
------------------------------------------------------------
RED    | time remaining: [#####] 5s | intersection: CLOSED
RED    | time remaining: [####] 4s | intersection: CLOSED
RED    | time remaining: [###] 3s | intersection: CLOSED
RED    | time remaining: [##] 2s | intersection: CLOSED
RED    | time remaining: [#] 1s | intersection: CLOSED
GREEN  | time remaining: [####] 4s | intersection: OPEN
GREEN  | time remaining: [###] 3s | intersection: OPEN
GREEN  | time remaining: [##] 2s | intersection: OPEN
GREEN  | time remaining: [#] 1s | intersection: OPEN
YELLOW | time remaining: [#] 1s | intersection: OPEN
... (cycle repeats)
```
