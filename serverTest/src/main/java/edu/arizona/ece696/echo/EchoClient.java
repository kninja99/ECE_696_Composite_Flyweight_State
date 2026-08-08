package edu.arizona.ece696.echo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * A tiny echo client for smoke-testing the servers by hand (JMeter does the real
 * load testing). It connects, sends a word, reads the echoed bytes until the
 * server closes the connection, and prints what came back plus the round-trip
 * time. Send a word of 4+ characters (e.g. {@code quit}) and the round trip
 * should be at least {@value EchoProtocol#OVERHEAD_MILLIS} ms, demonstrating the
 * simulated overhead.
 *
 * <p>Usage: {@code java edu.arizona.ece696.echo.EchoClient <server> <word> [<port>]}
 * (default port 7).</p>
 */
public final class EchoClient {

    private EchoClient() {
    }

    public static void main(String[] args) throws IOException {

        if (args.length < 2 || args.length > 3) { // Test for correct # of args
            throw new IllegalArgumentException("Parameter(s): <Server> <Word> [<Port>]");
        }

        String server = args[0];             // Server name or IP address
        byte[] data = args[1].getBytes();    // Word to echo, default charset
        int servPort = (args.length == 3) ? Integer.parseInt(args[2]) : 7;

        long start = System.nanoTime();

        // Create socket that is connected to server on the specified port
        try (Socket socket = new Socket(server, servPort)) {
            System.out.println("Connected to server... sending echo string");

            InputStream in = socket.getInputStream();
            OutputStream out = socket.getOutputStream();

            out.write(data); // Send the encoded string to the server
            out.flush();

            // Read the echo back. The server closes the connection after the
            // overhead, so read until end-of-stream (-1) rather than a fixed
            // count; this also tolerates the reply arriving in several chunks.
            byte[] buf = new byte[Math.max(data.length, 32)];
            StringBuilder echoed = new StringBuilder();
            int n;
            while ((n = in.read(buf)) != -1) {
                echoed.append(new String(buf, 0, n));
            }

            long elapsedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("Received: " + echoed);
            System.out.println("Round-trip time: " + elapsedMs + " ms");
        }
    }
}
