package edu.arizona.ece696.echo;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The per-connection echo logic, shared by all three server strategies.
 *
 * <p>This is Dr. Donahoo's {@code EchoProtocol} (from <em>TCP/IP Sockets in
 * Java</em>) with one addition: a <strong>simulated overhead</strong>. Whenever a
 * single read returns {@value #QUIT_THRESHOLD} or more bytes, the handler sleeps
 * {@value #OVERHEAD_MILLIS} ms and then closes the connection. That models the
 * cost of "real" per-request work (a database call, disk I/O, encryption, ...)
 * without needing any, and it is what makes the three concurrency strategies
 * measurably different under load: a single-threaded server must pay the 100 ms
 * serially for every client, whereas the threaded and pooled servers overlap it.</p>
 *
 * <p>Because the overhead lives here in the protocol rather than in any one
 * server, every strategy exercises exactly the same work per connection.</p>
 *
 * <p>The class implements {@link Runnable} so it can be handed straight to an
 * {@link java.util.concurrent.ExecutorService} via {@code execute(...)}.</p>
 */
public class EchoProtocol implements Runnable {

    /** Size (in bytes) of the receive buffer. */
    private static final int BUFSIZE = 32;

    /** A read of this many bytes (or more) triggers the simulated overhead + quit. */
    public static final int QUIT_THRESHOLD = 4;

    /** Simulated per-connection processing overhead, in milliseconds. */
    public static final long OVERHEAD_MILLIS = 100;

    private final Socket clntSock; // Socket connected to client
    private final Logger logger;   // Server logger

    public EchoProtocol(Socket clntSock, Logger logger) {
        this.clntSock = clntSock;
        this.logger = logger;
    }

    /**
     * Echoes bytes back to the client until it either sends a message of
     * {@link #QUIT_THRESHOLD}+ bytes (after which the server pauses
     * {@link #OVERHEAD_MILLIS} ms and hangs up) or closes the connection itself.
     */
    public static void handleEchoClient(Socket clntSock, Logger logger) {
        try {
            // Get the input and output I/O streams from socket
            InputStream in = clntSock.getInputStream();
            OutputStream out = clntSock.getOutputStream();

            int recvMsgSize;                        // Size of received message
            int totalBytesEchoed = 0;               // Bytes echoed to client
            byte[] echoBuffer = new byte[BUFSIZE];  // Receive buffer

            // Receive until client closes connection, indicated by -1 return
            while ((recvMsgSize = in.read(echoBuffer)) != -1) {
                out.write(echoBuffer, 0, recvMsgSize); // Echo it straight back
                out.flush();
                totalBytesEchoed += recvMsgSize;

                if (recvMsgSize >= QUIT_THRESHOLD) {
                    // Simulated processing overhead, then quit (break => close).
                    try {
                        Thread.sleep(OVERHEAD_MILLIS);
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                    }
                    break;
                }
            }

            logger.info("Client " + clntSock.getRemoteSocketAddress() + ", echoed "
                    + totalBytesEchoed + " bytes.");

        } catch (IOException ex) {
            logger.log(Level.WARNING, "Exception in echo protocol", ex);
        } finally {
            try {
                clntSock.close();
            } catch (IOException ignored) {
                // Nothing useful to do if close fails.
            }
        }
    }

    @Override
    public void run() {
        handleEchoClient(clntSock, logger);
    }
}
