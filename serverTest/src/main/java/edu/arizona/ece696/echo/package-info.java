/**
 * TCP echo server under three concurrency strategies, all built on
 * {@link java.util.concurrent.ExecutorService}.
 *
 * <p>Two of the three servers are reworked so that the accept loop hands each
 * connection to an {@code ExecutorService} instead of a raw {@code new Thread(...)}.
 * The thread-per-connection server is the deliberate exception: no executor
 * implements a genuine one-fresh-thread-per-connection policy (a cached pool reuses
 * idle threads; a fixed pool bounds them), so it keeps Donahoo's original raw
 * {@code Thread} &mdash; see
 * {@link edu.arizona.ece696.echo.ThreadPerConnectionEchoServer} for the full
 * justification. Each strategy is a separate server class:</p>
 *
 * <table border="1">
 *   <caption>Strategy to concurrency mechanism</caption>
 *   <tr><th>Class</th><th>Donahoo original</th><th>Concurrency mechanism</th></tr>
 *   <tr><td>{@link edu.arizona.ece696.echo.SingleThreadEchoServer}</td>
 *       <td>{@code TCPEchoServer}</td>
 *       <td>{@code Executors.newSingleThreadExecutor()}</td></tr>
 *   <tr><td>{@link edu.arizona.ece696.echo.ThreadPerConnectionEchoServer}</td>
 *       <td>{@code TCPEchoServerThread}</td>
 *       <td>raw {@code new Thread(...)} per connection (no executor exists for this)</td></tr>
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
