package edu.arizona.ece696.echo;

import java.awt.BasicStroke;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.ToDoubleFunction;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.LogAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

/**
 * Turns Apache JMeter result files into comparison graphs for the three echo
 * server strategies.
 *
 * <p>Point it at a directory of JMeter {@code .jtl} files (CSV format, the
 * default) named {@code <STRATEGY>_<clients>.jtl}, for example
 * {@code SINGLE_50.jtl}, {@code THREADPERCONN_100.jtl}, {@code POOL_500.jtl}.
 * For each file it computes throughput, mean response time and error rate, then
 * writes:</p>
 *
 * <ul>
 *   <li>{@code summary.csv} &ndash; one row per (strategy, client count);</li>
 *   <li>{@code throughput.png} &ndash; throughput vs. client count, one labelled
 *       line per strategy with a legend;</li>
 *   <li>{@code latency.png} &ndash; mean response time vs. client count, likewise.</li>
 * </ul>
 *
 * <p>The X axis is logarithmic so the {@code 5, 10, 50, 100, 500} client counts
 * are readable. Usage:
 * {@code java edu.arizona.ece696.echo.ChartGenerator [resultsDir]} (default
 * {@code results}).</p>
 */
public final class ChartGenerator {

    private static final int CHART_WIDTH = 900;
    private static final int CHART_HEIGHT = 600;

    /**
     * Maps the strategy token used in file names to a human-readable series name.
     * The insertion order also fixes the legend/plot order and colour assignment.
     */
    private static final Map<String, String> STRATEGY_NAMES = new LinkedHashMap<>();
    static {
        STRATEGY_NAMES.put("SINGLE", "Single-threaded");
        STRATEGY_NAMES.put("THREADPERCONN", "Thread-per-connection");
        // Thread pools are keyed POOL<size> (e.g. POOL10, POOL50) so several pool
        // sizes can be compared as separate series. Any POOL<n> not listed here is
        // still handled by displayName() below.
        STRATEGY_NAMES.put("POOL10", "Thread pool (10)");
        STRATEGY_NAMES.put("POOL50", "Thread pool (50)");
    }

    /** Recognises a POOL<size> strategy key, capturing the size. */
    private static final java.util.regex.Pattern POOL_KEY =
            java.util.regex.Pattern.compile("^POOL(\\d+)$");

    /** Distinct colours per strategy, in the same order as {@link #STRATEGY_NAMES}. */
    private static final Color[] SERIES_COLORS = {
        new Color(0xD1495B), // red    - single-threaded
        new Color(0x2E86AB), // blue   - thread-per-connection
        new Color(0x0EAD69), // green  - thread pool (10)
        new Color(0xE8871E), // orange - thread pool (50)
        new Color(0x8E44AD), // purple - spare (extra pool sizes)
        new Color(0x16A085)  // teal   - spare
    };

    /** Human-readable series name for a strategy key, incl. any POOL&lt;n&gt;. */
    private static String displayName(String key) {
        String known = STRATEGY_NAMES.get(key);
        if (known != null) {
            return known;
        }
        java.util.regex.Matcher m = POOL_KEY.matcher(key);
        if (m.matches()) {
            return "Thread pool (" + m.group(1) + ")";
        }
        return key;
    }

    private ChartGenerator() {
    }

    /**
     * One aggregated result: metrics for a single (strategy, clientCount) run.
     *
     * <p>Latency and throughput are computed over <em>successful</em> samples
     * only. Failed samples (connection refused, timeouts) are not real service
     * times: counting them would drag the mean response time around and, worse,
     * inflate throughput (a bottleneck fails fast, so failures would masquerade
     * as high throughput). {@code meanLatencyMs} is {@code NaN} when a cell had no
     * successful samples at all, so it is left off the chart rather than drawn as
     * a misleading zero.</p>
     */
    private static final class RunStats {
        final String strategyKey;
        final int clients;
        final long samples;          // total samples (successes + failures)
        final long successSamples;   // samples with success=true
        final long errors;           // samples with success=false
        final double throughputPerSec; // goodput: successSamples / test window
        final double meanLatencyMs;    // mean elapsed over successes, or NaN

        RunStats(String strategyKey, int clients, long samples, long successSamples,
                 long errors, double throughputPerSec, double meanLatencyMs) {
            this.strategyKey = strategyKey;
            this.clients = clients;
            this.samples = samples;
            this.successSamples = successSamples;
            this.errors = errors;
            this.throughputPerSec = throughputPerSec;
            this.meanLatencyMs = meanLatencyMs;
        }

        double errorPct() {
            return samples == 0 ? 0.0 : (100.0 * errors) / samples;
        }
    }

    public static void main(String[] args) throws IOException {
        File resultsDir = new File(args.length >= 1 ? args[0] : "results");
        if (!resultsDir.isDirectory()) {
            System.err.println("Results directory not found: " + resultsDir.getAbsolutePath()
                    + "\nRun the load test first (see run_load_tests.ps1).");
            System.exit(1);
        }

        File[] jtlFiles = resultsDir.listFiles(
                (dir, name) -> name.toLowerCase(Locale.ROOT).endsWith(".jtl"));
        if (jtlFiles == null || jtlFiles.length == 0) {
            System.err.println("No .jtl files in " + resultsDir.getAbsolutePath());
            System.exit(1);
        }

        List<RunStats> stats = new ArrayList<>();
        for (File jtl : jtlFiles) {
            RunStats rs = parseJtl(jtl);
            if (rs != null) {
                stats.add(rs);
                System.out.printf(Locale.ROOT,
                        "Parsed %-28s clients=%-4d samples=%-6d ok=%-6d goodput=%7.2f req/s  meanRT=%8.2f ms  err=%.1f%%%n",
                        jtl.getName(), rs.clients, rs.samples, rs.successSamples,
                        rs.throughputPerSec, rs.meanLatencyMs, rs.errorPct());
            }
        }
        if (stats.isEmpty()) {
            System.err.println("No parseable JMeter CSV results were found.");
            System.exit(1);
        }

        writeSummaryCsv(new File(resultsDir, "summary.csv"), stats);

        XYSeriesCollection throughput = buildDataset(stats, rs -> rs.throughputPerSec);
        XYSeriesCollection latency = buildDataset(stats, rs -> rs.meanLatencyMs);
        XYSeriesCollection errorRate = buildDataset(stats, RunStats::errorPct);

        saveChart(new File(resultsDir, "throughput.png"),
                "Echo Server Throughput vs. Concurrent Clients",
                "Concurrent clients", "Throughput (successful requests / second)", throughput);
        saveChart(new File(resultsDir, "latency.png"),
                "Echo Server Response Time vs. Concurrent Clients",
                "Concurrent clients", "Mean response time of successes (ms)", latency);
        saveChart(new File(resultsDir, "errorrate.png"),
                "Echo Server Error Rate vs. Concurrent Clients",
                "Concurrent clients", "Failed requests (%)", errorRate);

        System.out.println("Wrote summary.csv, throughput.png, latency.png and errorrate.png to "
                + resultsDir.getAbsolutePath());
    }

    /**
     * Reads one JMeter CSV {@code .jtl} file and aggregates it. Returns
     * {@code null} (with a warning) if the file cannot be interpreted.
     */
    private static RunStats parseJtl(File file) throws IOException {
        String base = file.getName();
        base = base.substring(0, base.length() - ".jtl".length());
        int us = base.lastIndexOf('_');
        if (us < 0) {
            System.err.println("Skipping " + file.getName()
                    + " (name is not <STRATEGY>_<clients>.jtl)");
            return null;
        }
        String strategyKey = base.substring(0, us).toUpperCase(Locale.ROOT);
        int clients;
        try {
            clients = Integer.parseInt(base.substring(us + 1).trim());
        } catch (NumberFormatException ex) {
            System.err.println("Skipping " + file.getName() + " (no client count in name)");
            return null;
        }

        try (BufferedReader in = Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8)) {
            String header = in.readLine();
            if (header == null) {
                System.err.println("Skipping empty file " + file.getName());
                return null;
            }
            // Tolerate a leading UTF-8 byte-order mark on the header line.
            if (!header.isEmpty() && header.charAt(0) == '﻿') {
                header = header.substring(1);
            }
            String[] cols = header.split(",", -1);
            int iTime = indexOf(cols, "timeStamp");
            int iElapsed = indexOf(cols, "elapsed");
            int iSuccess = indexOf(cols, "success");
            if (iTime < 0 || iElapsed < 0) {
                System.err.println("Skipping " + file.getName()
                        + " (not JMeter CSV output - missing timeStamp/elapsed header)");
                return null;
            }

            long samples = 0;
            long successSamples = 0;
            long errors = 0;
            double elapsedSumSuccess = 0; // sum of elapsed for successful samples only
            long minStart = Long.MAX_VALUE;
            long maxEnd = Long.MIN_VALUE;

            String line;
            while ((line = in.readLine()) != null) {
                if (line.isEmpty()) {
                    continue;
                }
                String[] f = line.split(",", -1);
                if (f.length <= iElapsed) {
                    continue; // malformed row
                }
                long ts;
                long elapsed;
                try {
                    ts = Long.parseLong(f[iTime].trim());
                    elapsed = Long.parseLong(f[iElapsed].trim());
                } catch (NumberFormatException ex) {
                    continue; // header repeated or junk row
                }
                samples++;
                // The whole test window (all samples) is the throughput denominator,
                // matching JMeter's own definition.
                minStart = Math.min(minStart, ts);
                maxEnd = Math.max(maxEnd, ts + elapsed);

                // A sample is a success unless there is a success column saying false.
                boolean success = !(iSuccess >= 0 && iSuccess < f.length
                        && !"true".equalsIgnoreCase(f[iSuccess].trim()));
                if (success) {
                    successSamples++;
                    elapsedSumSuccess += elapsed; // latency: successes only
                } else {
                    errors++;
                }
            }

            if (samples == 0) {
                System.err.println("Skipping " + file.getName() + " (no sample rows)");
                return null;
            }

            double windowSec = Math.max(1.0, (maxEnd - minStart)) / 1000.0;
            // Goodput: only successful requests count toward throughput, so a
            // bottleneck that fails fast shows up as a drop, not a phantom spike.
            double throughput = successSamples / windowSec;
            double meanLatency = successSamples > 0
                    ? elapsedSumSuccess / successSamples
                    : Double.NaN;
            return new RunStats(strategyKey, clients, samples, successSamples, errors,
                    throughput, meanLatency);
        }
    }

    private static int indexOf(String[] cols, String name) {
        for (int i = 0; i < cols.length; i++) {
            if (cols[i].trim().equalsIgnoreCase(name)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Builds an XY dataset: one series per strategy, X = clients, Y = the chosen
     * metric. Points whose value is {@code NaN} are skipped, so a cell with no
     * successful samples leaves a gap rather than a misleading zero.
     */
    private static XYSeriesCollection buildDataset(List<RunStats> stats,
                                                   ToDoubleFunction<RunStats> metric) {
        // strategyKey -> (clients -> value), TreeMap keeps client counts ascending.
        Map<String, TreeMap<Integer, Double>> byStrategy = new LinkedHashMap<>();
        for (String key : STRATEGY_NAMES.keySet()) {
            byStrategy.put(key, new TreeMap<>());
        }
        for (RunStats rs : stats) {
            double v = metric.applyAsDouble(rs);
            if (Double.isNaN(v)) {
                continue;
            }
            byStrategy.computeIfAbsent(rs.strategyKey, k -> new TreeMap<>())
                    .put(rs.clients, v);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        // Emit known strategies first (fixed order/colour), then any extras.
        for (Map.Entry<String, TreeMap<Integer, Double>> e : byStrategy.entrySet()) {
            if (e.getValue().isEmpty()) {
                continue;
            }
            String display = displayName(e.getKey());
            XYSeries series = new XYSeries(display);
            for (Map.Entry<Integer, Double> p : e.getValue().entrySet()) {
                series.add((double) p.getKey(), p.getValue());
            }
            dataset.addSeries(series);
        }
        return dataset;
    }

    private static void saveChart(File out, String title, String xLabel, String yLabel,
                                  XYSeriesCollection dataset) throws IOException {
        JFreeChart chart = ChartFactory.createXYLineChart(
                title, xLabel, yLabel, dataset,
                PlotOrientation.VERTICAL, true, false, false);

        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Logarithmic X axis so 5..500 clients spread out evenly.
        LogAxis xAxis = new LogAxis(xLabel);
        xAxis.setBase(10.0);
        xAxis.setNumberFormatOverride(plainIntegerFormat());
        plot.setDomainAxis(xAxis);

        NumberAxis yAxis = (NumberAxis) plot.getRangeAxis();
        yAxis.setAutoRangeIncludesZero(true);

        // Lines with visible markers at each measured client count.
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, true);
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, SERIES_COLORS[i % SERIES_COLORS.length]);
            renderer.setSeriesStroke(i, new BasicStroke(2.4f));
        }
        plot.setRenderer(renderer);

        ChartUtils.saveChartAsPNG(out, chart, CHART_WIDTH, CHART_HEIGHT);
    }

    /** Plain integer formatting for the log axis tick labels (5, 10, 50, ...). */
    private static java.text.NumberFormat plainIntegerFormat() {
        java.text.NumberFormat nf = java.text.NumberFormat.getIntegerInstance(Locale.ROOT);
        nf.setGroupingUsed(false);
        return nf;
    }

    private static void writeSummaryCsv(File out, List<RunStats> stats) throws IOException {
        // Sort by strategy order then client count for a tidy, stable file.
        stats.sort((a, b) -> {
            int oa = orderOf(a.strategyKey);
            int ob = orderOf(b.strategyKey);
            if (oa != ob) {
                return Integer.compare(oa, ob);
            }
            return Integer.compare(a.clients, b.clients);
        });
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(out.toPath(),
                StandardCharsets.UTF_8))) {
            // goodputPerSec and meanRespMs are computed over SUCCESSFUL samples only.
            pw.println("strategy,clients,samples,successes,errors,errorPct,goodputPerSec,meanRespMs");
            for (RunStats rs : stats) {
                String meanResp = Double.isNaN(rs.meanLatencyMs)
                        ? "" // no successful samples
                        : String.format(Locale.ROOT, "%.2f", rs.meanLatencyMs);
                pw.printf(Locale.ROOT, "%s,%d,%d,%d,%d,%.2f,%.2f,%s%n",
                        displayName(rs.strategyKey),
                        rs.clients, rs.samples, rs.successSamples, rs.errors, rs.errorPct(),
                        rs.throughputPerSec, meanResp);
            }
        }
    }

    private static int orderOf(String strategyKey) {
        int i = 0;
        for (String key : STRATEGY_NAMES.keySet()) {
            if (key.equals(strategyKey)) {
                return i;
            }
            i++;
        }
        return Integer.MAX_VALUE; // unknown strategies sort last
    }
}
