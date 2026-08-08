/**
 * TCP echo server under three concurrency strategies, all built on
 * {@link java.util.concurrent.ExecutorService}.
 *
 * <p>The classic Donahoo echo server is reworked so that the accept loop hands
 * each connection to an executor instead of a raw {@code new Thread(...)}. Each
 * strategy is a separate server class differing only in which executor it uses:</p>
 *
 * <table border="1">
 *   <caption>Strategy to executor mapping</caption>
 *   <tr><th>Class</th><th>Donahoo original</th><th>ExecutorService</th></tr>
 *   <tr><td>{@link edu.arizona.ece696.echo.SingleThreadEchoServer}</td>
 *       <td>{@code TCPEchoServer}</td>
 *       <td>{@code Executors.newSingleThreadExecutor()}</td></tr>
 *   <tr><td>{@link edu.arizona.ece696.echo.ThreadPerConnectionEchoServer}</td>
 *       <td>{@code TCPEchoServerThread}</td>
 *       <td>{@code Executors.newCachedThreadPool()}</td></tr>
 *   <tr><td>{@link edu.arizona.ece696.echo.ThreadPoolEchoServer}</td>
 *       <td>{@code TCPEchoServerPool}</td>
 *       <td>{@code Executors.newFixedThreadPool(n)}</td></tr>
 * </table>
 *
 * <p>All three run the shared {@link edu.arizona.ece696.echo.EchoProtocol},
 * which echoes bytes back and, on any read of 4 or more bytes, sleeps 100 ms to
 * simulate per-connection processing overhead before closing the connection.
 * {@link edu.arizona.ece696.echo.EchoClient} is a smoke-test client and
 * {@link edu.arizona.ece696.echo.ChartGenerator} turns JMeter results into
 * comparison graphs.</p>
 */
package edu.arizona.ece696.echo;
