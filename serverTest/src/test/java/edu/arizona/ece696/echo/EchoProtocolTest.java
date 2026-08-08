package edu.arizona.ece696.echo;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

/**
 * Exercises {@link EchoProtocol} over a real loopback socket pair: the two
 * behaviours the whole assignment hinges on are that bytes are echoed back
 * unchanged, and that a message of {@link EchoProtocol#QUIT_THRESHOLD}+ bytes
 * incurs the {@link EchoProtocol#OVERHEAD_MILLIS} ms overhead and then closes the
 * connection.
 */
@Timeout(10)
class EchoProtocolTest {

    private static final Logger QUIET_LOGGER = Logger.getLogger("test");
    static {
        QUIET_LOGGER.setLevel(Level.OFF);
    }

    private ServerSocket serverSocket;
    private ExecutorService serverExec;

    @BeforeEach
    void startProtocolServer() throws IOException {
        serverSocket = new ServerSocket(0); // ephemeral port
        serverExec = Executors.newSingleThreadExecutor();
    }

    @AfterEach
    void stop() throws IOException {
        serverExec.shutdownNow();
        serverSocket.close();
    }

    /** Accepts one connection on a background thread and runs the echo protocol. */
    private Future<?> acceptOnce() {
        return serverExec.submit(() -> {
            try {
                Socket accepted = serverSocket.accept();
                EchoProtocol.handleEchoClient(accepted, QUIET_LOGGER); // closes the socket
            } catch (IOException ex) {
                // Socket closed during teardown; nothing to assert here.
            }
        });
    }

    private int port() {
        return serverSocket.getLocalPort();
    }

    @Test
    @DisplayName("A message under 4 bytes is echoed back unchanged and without the overhead")
    void echoesShortMessageWithoutOverhead() throws Exception {
        acceptOnce();
        byte[] payload = "hi!".getBytes(StandardCharsets.UTF_8); // 3 bytes < threshold

        try (Socket client = new Socket(InetAddress.getLoopbackAddress(), port())) {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            long start = System.nanoTime();
            out.write(payload);
            out.flush();

            byte[] echo = readExactly(in, payload.length);
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            assertArrayEquals(payload, echo, "server must echo the bytes unchanged");
            assertTrue(elapsedMs < EchoProtocol.OVERHEAD_MILLIS,
                    "a sub-threshold message must not incur the " + EchoProtocol.OVERHEAD_MILLIS
                            + " ms overhead (took " + elapsedMs + " ms)");
        }
    }

    @Test
    @DisplayName("A message of 4+ bytes is echoed, then the server waits ~100 ms and closes")
    void appliesOverheadAndClosesOnFourPlusBytes() throws Exception {
        acceptOnce();
        byte[] payload = "quit".getBytes(StandardCharsets.UTF_8); // 4 bytes == threshold

        try (Socket client = new Socket(InetAddress.getLoopbackAddress(), port())) {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            long start = System.nanoTime();
            out.write(payload);
            out.flush();

            // Read everything until the server closes the connection (-1).
            ByteArrayOutputStream received = new ByteArrayOutputStream();
            byte[] buf = new byte[32];
            int n;
            while ((n = in.read(buf)) != -1) {
                received.write(buf, 0, n);
            }
            long elapsedMs = (System.nanoTime() - start) / 1_000_000;

            assertArrayEquals(payload, received.toByteArray(),
                    "server must echo the 4-byte message exactly once, then close");
            assertTrue(elapsedMs >= EchoProtocol.OVERHEAD_MILLIS - 10,
                    "a 4+ byte message must incur the ~" + EchoProtocol.OVERHEAD_MILLIS
                            + " ms overhead before the connection closes (took " + elapsedMs + " ms)");
        }
    }

    @Test
    @DisplayName("A longer message is echoed in full before the overhead + close")
    void echoesFullLongerMessage() throws Exception {
        acceptOnce();
        byte[] payload = "hello, echo".getBytes(StandardCharsets.UTF_8); // 11 bytes > threshold

        try (Socket client = new Socket(InetAddress.getLoopbackAddress(), port())) {
            OutputStream out = client.getOutputStream();
            InputStream in = client.getInputStream();

            out.write(payload);
            out.flush();

            byte[] echo = readExactly(in, payload.length);
            assertArrayEquals(payload, echo, "server must echo the full message unchanged");

            // The connection should now be closed by the server after its overhead.
            assertEquals(-1, in.read(), "server should close the connection after echoing");
        }
    }

    /** Reads exactly {@code len} bytes or fails if the stream ends early. */
    private static byte[] readExactly(InputStream in, int len) throws IOException {
        byte[] data = new byte[len];
        int off = 0;
        while (off < len) {
            int r = in.read(data, off, len - off);
            if (r == -1) {
                throw new IOException("stream closed after " + off + " of " + len + " bytes");
            }
            off += r;
        }
        return data;
    }
}
