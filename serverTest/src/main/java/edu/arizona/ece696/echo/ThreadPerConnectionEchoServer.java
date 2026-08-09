package edu.arizona.ece696.echo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Logger;

/**
 * <strong>Thread-per-connection</strong> echo server (Strategy 2).
 *
 * <p>Faithful to Dr. Donahoo's original {@code TCPEchoServerThread}: the accept
 * loop spawns a brand-new {@link Thread} for every accepted connection and neither
 * reuses nor bounds them.</p>
 *
 * <p><strong>Why this server keeps a raw {@code Thread} instead of an
 * {@link java.util.concurrent.ExecutorService}.</strong> The assignment asks
 * whether an {@code ExecutorService} exists that implements thread-per-connection.
 * It does not. The closest factory,
 * {@link java.util.concurrent.Executors#newCachedThreadPool()}, <em>reuses</em>
 * idle threads (it keeps each alive ~60&nbsp;s for the next task) instead of
 * creating exactly one new thread per connection, and
 * {@link java.util.concurrent.Executors#newFixedThreadPool(int)} <em>bounds</em>
 * concurrency to a fixed count. Neither matches the defining behaviour of the
 * thread-per-connection strategy &mdash; one fresh, unbounded thread per client.
 * A cached pool would arguably be more efficient, but it changes the strategy, so
 * per the assignment we use Donahoo's original raw-{@code Thread} implementation
 * here and reserve the {@code ExecutorService} approach for the single-threaded
 * and thread-pool servers.</p>
 *
 * <p>The simulated overhead still lives in the shared {@link EchoProtocol}, so this
 * server performs exactly the same per-connection work as the other two; only the
 * thread-management strategy differs. Its cost is unbounded thread growth: at very
 * high client counts the JVM eventually cannot create more threads.</p>
 *
 * <p>Usage: {@code java edu.arizona.ece696.echo.ThreadPerConnectionEchoServer <port>}</p>
 */
public final class ThreadPerConnectionEchoServer {

    private ThreadPerConnectionEchoServer() {
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int echoServPort = Integer.parseInt(args[0]); // Server port

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(echoServPort);

        Logger logger = Logger.getLogger("practical");

        logger.info("ThreadPerConnectionEchoServer listening on port " + echoServPort);

        // Run forever, spawning a brand-new thread for each connection (no reuse,
        // no bound) -- the classic thread-per-connection strategy.
        while (true) {
            Socket clntSock = servSock.accept(); // Block waiting for connection
            Thread thread = new Thread(new EchoProtocol(clntSock, logger));
            thread.start();
            logger.info("Created and started Thread " + thread.getName());
        }
        /* NOT REACHED */
    }
}
