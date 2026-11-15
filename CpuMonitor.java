package com.procura.monitoring;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Service that monitors system-wide CPU utilization, active physical/logical core counts,
 * and 1, 5, and 15-minute system load averages by reading from Linux {@code /proc/stat}
 * and {@code /proc/loadavg}.
 */
public class CpuMonitor {

    private final String procBasePath;
    private long previousTotalCpuTime = 0L;
    private long previousIdleCpuTime = 0L;

    /**
     * Constructs a CpuMonitor targeting the standard Linux {@code /proc} filesystem.
     */
    public CpuMonitor() {
        this("/proc");
    }

    /**
     * Constructs a CpuMonitor targeting a custom proc filesystem root (useful for unit testing).
     *
     * @param procBasePath Path to /proc directory.
     */
    public CpuMonitor(String procBasePath) {
        this.procBasePath = (procBasePath != null && !procBasePath.isEmpty()) ? procBasePath : "/proc";
        initializeBaseline();
    }

    /**
     * Samples the initial baseline CPU metrics.
     */
    private void initializeBaseline() {
        long[] sample = readCpuStatTimes();
        if (sample != null) {
            this.previousTotalCpuTime = sample[0];
            this.previousIdleCpuTime = sample[1];
        }
    }

    /**
     * Calculates the overall CPU usage percentage since the previous reading.
     * Computes the ratio of active time delta to total time delta from {@code /proc/stat}.
     *
     * @return CPU usage percentage in the range [0.0, 100.0].
     */
    public synchronized double getCpuUsagePercent() {
        long[] currentSample = readCpuStatTimes();

        if (currentSample == null) {
            // Fallback for non-Linux platforms using JVM OperatingSystemMXBean
            return getFallbackCpuUsage();
        }

        long currentTotal = currentSample[0];
        long currentIdle = currentSample[1];

        long deltaTotal = currentTotal - previousTotalCpuTime;
        long deltaIdle = currentIdle - previousIdleCpuTime;

        // If no time has elapsed between samples, perform an immediate short sleep sample
        if (deltaTotal <= 0) {
            try {
                Thread.sleep(60);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
            currentSample = readCpuStatTimes();
            if (currentSample != null) {
                currentTotal = currentSample[0];
                currentIdle = currentSample[1];
                deltaTotal = currentTotal - previousTotalCpuTime;
                deltaIdle = currentIdle - previousIdleCpuTime;
            }
        }

        double usagePercent = 0.0;
        if (deltaTotal > 0) {
            long deltaActive = Math.max(0L, deltaTotal - deltaIdle);
            usagePercent = (deltaActive / (double) deltaTotal) * 100.0;
            usagePercent = Math.min(100.0, Math.max(0.0, usagePercent));
            usagePercent = Math.round(usagePercent * 100.0) / 100.0;
        }

        this.previousTotalCpuTime = currentTotal;
        this.previousIdleCpuTime = currentIdle;

        return usagePercent;
    }

    /**
     * Returns the number of available CPU cores (logical processors).
     * First attempts to parse {@code /proc/cpuinfo}, falling back to {@link Runtime#availableProcessors()}.
     *
     * @return Number of CPU cores.
     */
    public int getCpuCoreCount() {
        Path cpuInfoPath = Paths.get(procBasePath, "cpuinfo");
        if (Files.exists(cpuInfoPath)) {
            try {
                List<String> lines = Files.readAllLines(cpuInfoPath, StandardCharsets.UTF_8);
                int count = 0;
                for (String line : lines) {
                    if (line.toLowerCase().startsWith("processor") && line.contains(":")) {
                        count++;
                    }
                }
                if (count > 0) {
                    return count;
                }
            } catch (IOException ignored) {
            }
        }
        return Runtime.getRuntime().availableProcessors();
    }

    /**
     * Reads the 1-minute, 5-minute, and 15-minute system load averages from {@code /proc/loadavg}.
     *
     * @return Array of 3 doubles representing [1-min, 5-min, 15-min] load averages.
     */
    public double[] getLoadAverage() {
        Path loadavgPath = Paths.get(procBasePath, "loadavg");
        if (Files.exists(loadavgPath)) {
            try {
                String content = Files.readString(loadavgPath, StandardCharsets.UTF_8).trim();
                String[] tokens = content.split("\\s+");
                if (tokens.length >= 3) {
                    double load1 = Double.parseDouble(tokens[0]);
                    double load5 = Double.parseDouble(tokens[1]);
                    double load15 = Double.parseDouble(tokens[2]);
                    return new double[]{load1, load5, load15};
                }
            } catch (Exception ignored) {
            }
        }

        // Fallback for non-Linux platforms
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        double avg = osBean.getSystemLoadAverage();
        if (avg < 0) {
            avg = 0.0;
        }
        return new double[]{avg, avg, avg};
    }

    /**
     * Reads total time and idle time from the aggregate {@code cpu} line in {@code /proc/stat}.
     * Line format: cpu  user nice system idle iowait irq softirq steal guest guest_nice
     *
     * @return Array [totalCpuTime, idleCpuTime] or null if file not found.
     */
    private long[] readCpuStatTimes() {
        Path statPath = Paths.get(procBasePath, "stat");
        if (!Files.exists(statPath)) {
            return null;
        }

        try {
            List<String> lines = Files.readAllLines(statPath, StandardCharsets.UTF_8);
            for (String line : lines) {
                if (line.startsWith("cpu ")) {
                    String[] tokens = line.trim().split("\\s+");
                    // tokens[0] is "cpu"
                    // user=1, nice=2, system=3, idle=4, iowait=5, irq=6, softirq=7, steal=8, guest=9, guest_nice=10
                    long user = parseLongSafe(tokens, 1);
                    long nice = parseLongSafe(tokens, 2);
                    long system = parseLongSafe(tokens, 3);
                    long idle = parseLongSafe(tokens, 4);
                    long iowait = parseLongSafe(tokens, 5);
                    long irq = parseLongSafe(tokens, 6);
                    long softirq = parseLongSafe(tokens, 7);
                    long steal = parseLongSafe(tokens, 8);

                    long total = user + nice + system + idle + iowait + irq + softirq + steal;
                    long idleTotal = idle + iowait;

                    return new long[]{total, idleTotal};
                }
            }
        } catch (Exception ignored) {
        }
        return null;
    }

    private long parseLongSafe(String[] tokens, int index) {
        if (index < tokens.length) {
            try {
                return Long.parseLong(tokens[index]);
            } catch (NumberFormatException ignored) {
            }
        }
        return 0L;
    }

    /**
     * Fallback CPU usage calculation using Java ManagementFactory.
     */
    private double getFallbackCpuUsage() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                double load = sunBean.getCpuLoad();
                if (load >= 0) {
                    return Math.round(load * 10000.0) / 100.0;
                }
            }
        } catch (Throwable ignored) {
        }
        return 0.0;
    }
}
