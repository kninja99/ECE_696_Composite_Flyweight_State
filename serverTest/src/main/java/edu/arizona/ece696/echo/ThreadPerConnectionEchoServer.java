package edu.arizona.ece696.echo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * <strong>Thread-per-connection</strong> echo server (Strategy 2).
 *
 * <p>Executor-based rework of Donahoo's {@code TCPEchoServerThread}. Each accepted
 * connection is submitted to a {@link Executors#newCachedThreadPool() cached
 * thread pool}, which creates a new thread on demand (reusing idle ones) and so
 * effectively runs one thread per concurrent connection. The 100 ms overhead is
 * paid in parallel, so latency stays near 100 ms and throughput scales with the
 * client count &mdash; until the sheer number of threads becomes the bottleneck
 * (context-switching and memory), which the load test is designed to expose.</p>
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

        // Strategy: cached pool => a new thread per connection, reused when idle.
        ExecutorService service = Executors.newCachedThreadPool();

        logger.info("ThreadPerConnectionEchoServer listening on port " + echoServPort);

        // Run forever, accepting connections and spawning a thread for each
        while (true) {
            Socket clntSock = servSock.accept(); // Block waiting for connection
            service.execute(new EchoProtocol(clntSock, logger));
        }
        /* NOT REACHED */
    }
}
