package com.procura.monitoring;

import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Service that monitors physical RAM and Swap memory utilization by parsing
 * the Linux {@code /proc/meminfo} virtual file.
 */
public class MemoryMonitor {

    private final String procBasePath;
    private static final Pattern MEMINFO_LINE_PATTERN = Pattern.compile("^([A-Za-z0-9_()]+):\\s*(\\d+)(?:\\s*kB)?");

    /**
     * Constructs a MemoryMonitor targeting the standard Linux {@code /proc} directory.
     */
    public MemoryMonitor() {
        this("/proc");
    }

    /**
     * Constructs a MemoryMonitor targeting a custom proc directory (useful for unit testing).
     *
     * @param procBasePath Path to /proc root directory.
     */
    public MemoryMonitor(String procBasePath) {
        this.procBasePath = (procBasePath != null && !procBasePath.isEmpty()) ? procBasePath : "/proc";
    }

    /**
     * Retrieves the total physical RAM in Kilobytes.
     *
     * @return Total memory in KB.
     */
    public long getTotalMemoryKb() {
        Map<String, Long> meminfo = parseMemInfo();
        if (meminfo.containsKey("MemTotal")) {
            return meminfo.get("MemTotal");
        }
        return getFallbackTotalMemoryKb();
    }

    /**
     * Retrieves the free physical RAM in Kilobytes.
     *
     * @return Free memory in KB.
     */
    public long getFreeMemoryKb() {
        Map<String, Long> meminfo = parseMemInfo();
        if (meminfo.containsKey("MemFree")) {
            return meminfo.get("MemFree");
        }
        return getFallbackFreeMemoryKb();
    }

    /**
     * Retrieves the estimated available physical RAM in Kilobytes without swapping.
     *
     * @return Available memory in KB.
     */
    public long getAvailableMemoryKb() {
        Map<String, Long> meminfo = parseMemInfo();
        if (meminfo.containsKey("MemAvailable")) {
            return meminfo.get("MemAvailable");
        }
        // Approximate available memory on older kernels: MemFree + Buffers + Cached
        long free = meminfo.getOrDefault("MemFree", 0L);
        long buffers = meminfo.getOrDefault("Buffers", 0L);
        long cached = meminfo.getOrDefault("Cached", 0L);
        long approx = free + buffers + cached;
        return approx > 0 ? approx : getFreeMemoryKb();
    }

    /**
     * Retrieves the used physical RAM in Kilobytes.
     * Defined standardly as: {@code MemTotal - MemAvailable} (or {@code MemTotal - Free - Buffers - Cached}).
     *
     * @return Used memory in KB.
     */
    public long getUsedMemoryKb() {
        long total = getTotalMemoryKb();
        long available = getAvailableMemoryKb();
        return Math.max(0L, total - available);
    }

    /**
     * Calculates the memory usage percentage based on used memory relative to total memory.
     *
     * @return Memory usage percentage [0.0, 100.0].
     */
    public double getMemoryUsagePercent() {
        long total = getTotalMemoryKb();
        long used = getUsedMemoryKb();
        if (total <= 0) {
            return 0.0;
        }
        double percent = (used / (double) total) * 100.0;
        percent = Math.min(100.0, Math.max(0.0, percent));
        return Math.round(percent * 100.0) / 100.0;
    }

    /**
     * Retrieves the total configured swap space in Kilobytes.
     *
     * @return Total swap in KB.
     */
    public long getSwapTotalKb() {
        Map<String, Long> meminfo = parseMemInfo();
        return meminfo.getOrDefault("SwapTotal", 0L);
    }

    /**
     * Retrieves the currently occupied swap space in Kilobytes.
     *
     * @return Used swap in KB.
     */
    public long getSwapUsedKb() {
        Map<String, Long> meminfo = parseMemInfo();
        long swapTotal = meminfo.getOrDefault("SwapTotal", 0L);
        long swapFree = meminfo.getOrDefault("SwapFree", 0L);
        return Math.max(0L, swapTotal - swapFree);
    }

    /**
     * Parses key-value pairs from {@code /proc/meminfo}.
     *
     * @return Map of metric key to value in Kilobytes.
     */
    private Map<String, Long> parseMemInfo() {
        Map<String, Long> map = new HashMap<>();
        Path meminfoPath = Paths.get(procBasePath, "meminfo");
        if (Files.exists(meminfoPath)) {
            try {
                List<String> lines = Files.readAllLines(meminfoPath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    Matcher matcher = MEMINFO_LINE_PATTERN.matcher(line.trim());
                    if (matcher.find()) {
                        String key = matcher.group(1);
                        long val = Long.parseLong(matcher.group(2));
                        map.put(key, val);
                    }
                }
            } catch (IOException ignored) {
            }
        }
        return map;
    }

    /**
     * Fallback total memory for non-Linux platforms via JVM OperatingSystemMXBean.
     */
    private long getFallbackTotalMemoryKb() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                long bytes = sunBean.getTotalMemorySize();
                return bytes > 0 ? bytes / 1024L : 0L;
            }
        } catch (Throwable ignored) {
        }
        return 8L * 1024L * 1024L; // Default fallback: 8GB
    }

    /**
     * Fallback free memory for non-Linux platforms via JVM OperatingSystemMXBean.
     */
    private long getFallbackFreeMemoryKb() {
        try {
            OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
            if (osBean instanceof com.sun.management.OperatingSystemMXBean sunBean) {
                long bytes = sunBean.getFreeMemorySize();
                return bytes > 0 ? bytes / 1024L : 0L;
            }
        } catch (Throwable ignored) {
        }
        return 4L * 1024L * 1024L;
    }
}
