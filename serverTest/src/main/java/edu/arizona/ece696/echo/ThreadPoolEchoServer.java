package edu.arizona.ece696.echo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * <strong>Thread-pool</strong> echo server (Strategy 3).
 *
 * <p>Executor-based rework of Donahoo's {@code TCPEchoServerPool}. Connections
 * are submitted to a {@link Executors#newFixedThreadPool(int) fixed thread pool}
 * of {@code <poolSize>} workers; extra connections wait in the executor's queue
 * until a worker is free. This <em>bounds</em> concurrency: throughput climbs to
 * about {@code poolSize * (1000 / 100) = poolSize * 10} connections/second and
 * then plateaus, while latency stays flat until the pool saturates and the queue
 * begins to build. It is the middle ground the load-test graphs highlight &mdash;
 * higher throughput than single-threaded, without the unbounded thread growth of
 * thread-per-connection.</p>
 *
 * <p>Usage: {@code java edu.arizona.ece696.echo.ThreadPoolEchoServer <port> <poolSize>}</p>
 */
public final class ThreadPoolEchoServer {

    private ThreadPoolEchoServer() {
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 2) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port> <PoolSize>");
        }

        int echoServPort = Integer.parseInt(args[0]);  // Server port
        int threadPoolSize = Integer.parseInt(args[1]); // Fixed pool size

        if (threadPoolSize < 1) {
            throw new IllegalArgumentException("<PoolSize> must be >= 1");
        }

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(echoServPort);

        Logger logger = Logger.getLogger("practical");

        // Strategy: fixed pool => at most threadPoolSize connections in flight.
        ExecutorService service = Executors.newFixedThreadPool(threadPoolSize);

        logger.info("ThreadPoolEchoServer listening on port " + echoServPort
                + " with a fixed pool of " + threadPoolSize + " threads");

        // Run forever, accepting connections and dispatching them to the pool
        while (true) {
            Socket clntSock = servSock.accept(); // Block waiting for connection
            service.execute(new EchoProtocol(clntSock, logger));
        }
        /* NOT REACHED */
    }
}
