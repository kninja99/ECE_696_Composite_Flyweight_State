package edu.arizona.ece696.echo;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

/**
 * <strong>Single-threaded</strong> echo server (Strategy 1).
 *
 * <p>Executor-based rework of Donahoo's {@code TCPEchoServer}. Every accepted
 * connection is submitted to a {@link Executors#newSingleThreadExecutor()
 * single-thread executor}, so connections are serviced strictly one at a time,
 * in arrival order. Under the {@link EchoProtocol}'s 100 ms overhead this means
 * the Nth waiting client waits roughly {@code N * 100 ms} &mdash; latency grows
 * linearly with load and throughput is capped near 10 connections/second.</p>
 *
 * <p>Usage: {@code java edu.arizona.ece696.echo.SingleThreadEchoServer <port>}</p>
 */
public final class SingleThreadEchoServer {

    private SingleThreadEchoServer() {
    }

    public static void main(String[] args) throws IOException {

        if (args.length != 1) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Port>");
        }

        int echoServPort = Integer.parseInt(args[0]); // Server port

        // Create a server socket to accept client connection requests
        ServerSocket servSock = new ServerSocket(echoServPort);

        Logger logger = Logger.getLogger("practical");

        // Strategy: one worker thread => connections are serviced serially.
        ExecutorService service = Executors.newSingleThreadExecutor();

        logger.info("SingleThreadEchoServer listening on port " + echoServPort);

        // Run forever, accepting connections and queueing them onto the executor
        while (true) {
            Socket clntSock = servSock.accept(); // Block waiting for connection
            service.execute(new EchoProtocol(clntSock, logger));
        }
        /* NOT REACHED */
    }
}
