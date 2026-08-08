# Echo Server Concurrency Strategies — Load Test & Comparison

An ECE 696 assignment. Starting from **Dr. Donahoo's TCP echo server** (*TCP/IP Sockets
in Java*), this project reworks it onto `java.util.concurrent.ExecutorService` under
**three concurrency strategies**, adds a **simulated per-connection overhead**, load-tests
all three with **Apache JMeter** at 5/10/50/100/500 concurrent clients, and renders
**comparison graphs** so you can see which strategy wins in which scenario.

## The idea

A shared echo protocol echoes bytes straight back, but whenever a single read returns
**4 or more bytes** it sleeps **100 ms** and then closes the connection. That 100 ms
stands in for "real" per-request work (a DB call, disk I/O, crypto). Because every
connection pays the same 100 ms, the only thing that differs between the servers is *how
they schedule concurrent connections* — which is exactly what we measure.

| Strategy (separate class)        | Donahoo original      | ExecutorService                        | Under load (100 ms overhead) |
|----------------------------------|-----------------------|----------------------------------------|------------------------------|
| `SingleThreadEchoServer`         | `TCPEchoServer`       | `Executors.newSingleThreadExecutor()`  | Serial: ~10 conn/s, latency grows with the queue |
| `ThreadPerConnectionEchoServer`  | `TCPEchoServerThread` | `Executors.newCachedThreadPool()`      | Fully parallel: ~flat latency, until thread count itself is the bottleneck |
| `ThreadPoolEchoServer`           | `TCPEchoServerPool`   | `Executors.newFixedThreadPool(n)`      | Bounded: throughput ≈ `n × 10` conn/s, then it plateaus and queues |

All three keep the classic `accept()` loop; each just hands the accepted socket to its
executor with `service.execute(new EchoProtocol(sock, logger))` instead of `new Thread(...)`.
That is how "three strategies" and "convert everything to `ExecutorService`" coexist.

## Project layout

```
serverTest/
├── pom.xml                       # edu.arizona.ece696:echo-server-strategies (Java 17)
├── README.md
├── run_load_tests.ps1            # driver: sweeps 3 servers × {5,10,50,100,500}, runs JMeter, makes charts
├── jmeter/
│   └── echo_load_test.jmx        # parameterised TCP-sampler load-test plan
├── src/main/java/edu/arizona/ece696/echo/
│   ├── EchoProtocol.java             # SHARED: echo + 100 ms/≥4-byte overhead (a Runnable)
│   ├── SingleThreadEchoServer.java   # Strategy 1  (newSingleThreadExecutor)
│   ├── ThreadPerConnectionEchoServer.java  # Strategy 2  (newCachedThreadPool)
│   ├── ThreadPoolEchoServer.java     # Strategy 3  (newFixedThreadPool)
│   ├── EchoClient.java               # smoke-test client (echo + round-trip timing)
│   └── ChartGenerator.java           # JMeter .jtl → summary.csv + throughput.png + latency.png
└── src/test/java/edu/arizona/ece696/echo/
    └── EchoProtocolTest.java         # JUnit 5: echo correctness + the 100 ms overhead
```

## Build & import (Eclipse, no command-line Maven needed)

1. **File ▸ Import… ▸ Maven ▸ Existing Maven Projects**, select the `serverTest` folder,
   **Finish**. Eclipse's bundled Maven (m2e) downloads **JFreeChart** and **JUnit 5** on
   the first build (needs internet once).
2. Run a server: right-click e.g. `ThreadPoolEchoServer.java` ▸ **Run As ▸ Java
   Application**. Set arguments via **Run ▸ Run Configurations… ▸ Arguments** (see below).
3. Run the tests: right-click `src/test/java` ▸ **Run As ▸ JUnit Test**.

### Command line (if Maven is installed)

```bash
mvn test                                                                          # run the JUnit tests
mvn compile exec:java -Dexec.mainClass=edu.arizona.ece696.echo.SingleThreadEchoServer -Dexec.arguments=5000
mvn compile exec:java -Dexec.mainClass=edu.arizona.ece696.echo.ChartGenerator      -Dexec.arguments=results
```

### Quick smoke test with only a JDK (no Maven)

From the `serverTest` folder (this compiles everything except `ChartGenerator`, which
needs JFreeChart):

```bash
javac -d out src/main/java/edu/arizona/ece696/echo/EchoProtocol.java src/main/java/edu/arizona/ece696/echo/SingleThreadEchoServer.java src/main/java/edu/arizona/ece696/echo/ThreadPerConnectionEchoServer.java src/main/java/edu/arizona/ece696/echo/ThreadPoolEchoServer.java src/main/java/edu/arizona/ece696/echo/EchoClient.java
```

## Running the servers by hand

| Server | Arguments | Example |
|--------|-----------|---------|
| `SingleThreadEchoServer`        | `<port>`            | `java -cp out edu.arizona.ece696.echo.SingleThreadEchoServer 5000` |
| `ThreadPerConnectionEchoServer` | `<port>`            | `java -cp out edu.arizona.ece696.echo.ThreadPerConnectionEchoServer 5000` |
| `ThreadPoolEchoServer`          | `<port> <poolSize>` | `java -cp out edu.arizona.ece696.echo.ThreadPoolEchoServer 5000 50` |

Then, from another terminal, exercise it with the client (a 4+ char word triggers the
overhead, so the round trip should be ≥ 100 ms):

```bash
java -cp out edu.arizona.ece696.echo.EchoClient localhost quit 5000
```

## Load testing with Apache JMeter

1. **Install JMeter**: download from <https://jmeter.apache.org/download_jmeter.cgi>,
   unzip, and add its `bin/` directory to your `PATH` (so `jmeter` runs from a terminal).
2. **Run the whole sweep** from the `serverTest` folder (PowerShell on Windows):

   ```powershell
   ./run_load_tests.ps1
   ```

   Optional parameters:

   ```powershell
   ./run_load_tests.ps1 -Port 5000 -PoolSize 50 -Duration 20 -Clients 5,10,50,100,500
   ```

   For each strategy × client count the script starts the server, runs
   `jmeter/echo_load_test.jmx` against it for `-Duration` seconds, stops the server, and
   writes `results/<STRATEGY>_<clients>.jtl`. It then generates the charts.

The plan is **time-bounded** (a fixed number of seconds per cell), not loop-bounded, so
the slow single-threaded server doesn't take minutes at 500 clients — every strategy is
measured over the same wall-clock window. Each JMeter sample opens a fresh TCP connection,
sends `quit`, and reads the echo until the server closes the socket.

### Running JMeter manually (one cell)

```bash
jmeter -n -t jmeter/echo_load_test.jmx -Jhost=localhost -Jport=5000 -Jclients=50 -Jduration=20 -l results/POOL_50.jtl
```

## Comparison graphs

`ChartGenerator` reads every `results/*.jtl`, infers the `(strategy, clients)` from each
file name, and writes to `results/`:

* **`summary.csv`** — `strategy, clients, samples, errors, errorPct, throughputPerSec, meanLatencyMs`
* **`throughput.png`** — throughput (requests/second) vs. concurrent clients
* **`latency.png`** — mean response time (ms) vs. concurrent clients

Both charts plot the client count on a **logarithmic X axis** (so 5…500 spread evenly),
draw **one labelled line per strategy**, and include a **legend** and titled axes.
`run_load_tests.ps1` calls it automatically; to run it yourself:

```bash
# in Eclipse: run ChartGenerator as a Java Application with program argument:  results
# or with Maven:
mvn compile exec:java -Dexec.mainClass=edu.arizona.ece696.echo.ChartGenerator -Dexec.arguments=results
```

## Reading the results

- **Few clients (5–10):** all three look similar — latency near 100 ms — because nobody is
  waiting behind anyone else.
- **Many clients (100–500):** the **single-threaded** server's latency climbs steeply (its
  throughput is pinned near 10 req/s and everyone queues), the **thread-pool** server holds
  a steady throughput around `poolSize × 10` req/s with moderate latency, and
  **thread-per-connection** keeps latency low as long as the JVM can afford one thread per
  client. The graphs make the crossover — and thread-per-connection's eventual cost at very
  high concurrency — visible. There is no single "best"; it depends on the expected load.
