package com.procura.monitoring;

import com.procura.util.SystemInfo;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service that inspects running processes by reading from the Linux {@code /proc} filesystem
 * or utilizing Java's {@link ProcessHandle} API when running in alternative environments.
 * Handles process lifecycle changes, process termination, and permission restrictions gracefully.
 */
public class ProcessMonitor {

    private final String procBasePath;
    private final Map<Long, Long> previousProcessCpuTimes = new ConcurrentHashMap<>();
    private long previousSystemCpuTime = 0L;
    private final int coreCount;

    // Pattern to parse VmRSS in /proc/[pid]/status
    private static final Pattern VMRSS_PATTERN = Pattern.compile("VmRSS:\\s*(\\d+)\\s*kB", Pattern.CASE_INSENSITIVE);
    private static final Pattern THREADS_PATTERN = Pattern.compile("Threads:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern UID_PATTERN = Pattern.compile("Uid:\\s*(\\d+)", Pattern.CASE_INSENSITIVE);

    /**
     * Constructs a default ProcessMonitor pointing to the standard Linux {@code /proc} filesystem.
     */
    public ProcessMonitor() {
        this("/proc");
    }

    /**
     * Constructs a ProcessMonitor with a custom /proc filesystem root path (useful for testing).
     *
     * @param procBasePath Path to /proc root directory.
     */
    public ProcessMonitor(String procBasePath) {
        this.procBasePath = (procBasePath != null && !procBasePath.isEmpty()) ? procBasePath : "/proc";
        this.coreCount = Math.max(1, Runtime.getRuntime().availableProcessors());
        // Initialize baseline total system CPU time
        this.previousSystemCpuTime = readSystemTotalCpuTime();
    }

    /**
     * Retrieves the list of currently active operating system processes.
     *
     * @return List of {@link ProcessInfo} records.
     */
    public List<ProcessInfo> getProcessList() {
        File procDir = new File(procBasePath);
        if (procDir.exists() && procDir.isDirectory()) {
            return getProcessListFromProc();
        } else {
            return getProcessListFromProcessHandle();
        }
    }

    /**
     * Retrieves detailed process information for a specific PID.
     *
     * @param pid Process Identifier.
     * @return {@link ProcessInfo} instance, or {@code null} if the process does not exist or cannot be read.
     */
    public ProcessInfo getProcessInfo(long pid) {
        File procPidDir = new File(procBasePath, String.valueOf(pid));
        if (procPidDir.exists() && procPidDir.isDirectory()) {
            return parseProcessFromProc(pid);
        }

        // Fallback using Java ProcessHandle API
        Optional<ProcessHandle> phOpt = ProcessHandle.of(pid);
        if (phOpt.isPresent()) {
            return convertHandleToProcessInfo(phOpt.get());
        }

        return null;
    }

    /**
     * Searches for a process matching the exact Process Identifier.
     *
     * @param pid Process ID.
     * @return Matching {@link ProcessInfo}, or {@code null} if not found.
     */
    public ProcessInfo searchByPid(long pid) {
        return getProcessInfo(pid);
    }

    /**
     * Searches for all processes whose name or command contains the search keyword (case-insensitive).
     *
     * @param name Name keyword or search query.
     * @return List of matching {@link ProcessInfo} instances.
     */
    public List<ProcessInfo> searchByName(String name) {
        List<ProcessInfo> all = getProcessList();
        if (name == null || name.trim().isEmpty()) {
            return all;
        }
        String query = name.trim().toLowerCase();
        return all.stream()
                .filter(p -> (p.getName() != null && p.getName().toLowerCase().contains(query))
                        || (p.getCommand() != null && p.getCommand().toLowerCase().contains(query)))
                .collect(Collectors.toList());
    }

    /**
     * Returns the total count of currently monitored processes.
     *
     * @return Total process count.
     */
    public int getProcessCount() {
        return getProcessList().size();
    }

    /**
     * Returns the count of processes currently in a Running state ('R').
     *
     * @return Number of running processes.
     */
    public int getRunningCount() {
        return (int) getProcessList().stream()
                .filter(p -> "R".equalsIgnoreCase(p.getState()) || p.getState().toLowerCase().startsWith("run"))
                .count();
    }

    /**
     * Returns the count of processes currently in a Sleeping state ('S' or 'D').
     *
     * @return Number of sleeping processes.
     */
    public int getSleepingCount() {
        return (int) getProcessList().stream()
                .filter(p -> "S".equalsIgnoreCase(p.getState()) || "D".equalsIgnoreCase(p.getState())
                        || p.getState().toLowerCase().startsWith("sleep"))
                .count();
    }

    /**
     * Calculates and updates the CPU usage percentages for all currently monitored processes
     * by comparing the delta between the previous sample and current sample from /proc/stat.
     */
    public void calculateCpuUsage() {
        long currentSystemCpu = readSystemTotalCpuTime();
        long deltaSystem = currentSystemCpu - previousSystemCpuTime;
        if (deltaSystem <= 0) {
            return;
        }

        List<ProcessInfo> list = getProcessList();
        for (ProcessInfo info : list) {
            long pid = info.getPid();
            long processCpuTicks = readProcessCpuTicks(pid);
            Long prevTicks = previousProcessCpuTimes.get(pid);

            if (prevTicks != null && processCpuTicks >= prevTicks) {
                long deltaProcess = processCpuTicks - prevTicks;
                double usage = (deltaProcess / (double) deltaSystem) * 100.0 * coreCount;
                usage = Math.min(100.0 * coreCount, Math.max(0.0, usage));
                usage = Math.round(usage * 100.0) / 100.0;
                info.setCpuUsage(usage);
            }
            previousProcessCpuTimes.put(pid, processCpuTicks);
        }

        this.previousSystemCpuTime = currentSystemCpu;
    }

    /**
     * Calculates the CPU usage percentage for a specific process PID between two sample readings.
     *
     * @param pid Process Identifier.
     * @return CPU usage percentage (0.0 to 100.0+), or 0.0 if not calculable.
     */
    public double calculateCpuUsage(long pid) {
        long currentSystemCpu = readSystemTotalCpuTime();
        long deltaSystem = currentSystemCpu - previousSystemCpuTime;
        long currentProcessTicks = readProcessCpuTicks(pid);
        Long prevTicks = previousProcessCpuTimes.get(pid);

        double usage = 0.0;
        if (prevTicks != null && deltaSystem > 0 && currentProcessTicks >= prevTicks) {
            long deltaProc = currentProcessTicks - prevTicks;
            usage = (deltaProc / (double) deltaSystem) * 100.0 * coreCount;
            usage = Math.min(100.0 * coreCount, Math.max(0.0, usage));
            usage = Math.round(usage * 100.0) / 100.0;
        }

        previousProcessCpuTimes.put(pid, currentProcessTicks);
        if (deltaSystem > 0) {
            this.previousSystemCpuTime = currentSystemCpu;
        }
        return usage;
    }

    /**
     * Reads and parses processes from the Linux /proc filesystem.
     */
    private List<ProcessInfo> getProcessListFromProc() {
        List<ProcessInfo> processes = new ArrayList<>();
        File procDir = new File(procBasePath);
        File[] pidDirs = procDir.listFiles((dir, name) -> name.matches("\\d+"));

        if (pidDirs == null) {
            return processes;
        }

        long currentSystemCpu = readSystemTotalCpuTime();
        long deltaSystem = currentSystemCpu - previousSystemCpuTime;

        for (File pidDir : pidDirs) {
            try {
                long pid = Long.parseLong(pidDir.getName());
                ProcessInfo info = parseProcessFromProc(pid);
                if (info != null) {
                    // Update CPU delta
                    long currentTicks = readProcessCpuTicks(pid);
                    Long prevTicks = previousProcessCpuTimes.get(pid);
                    if (prevTicks != null && deltaSystem > 0 && currentTicks >= prevTicks) {
                        double cpu = (currentTicks - prevTicks) / (double) deltaSystem * 100.0 * coreCount;
                        cpu = Math.min(100.0 * coreCount, Math.max(0.0, cpu));
                        info.setCpuUsage(Math.round(cpu * 100.0) / 100.0);
                    }
                    previousProcessCpuTimes.put(pid, currentTicks);
                    processes.add(info);
                }
            } catch (Exception ignored) {
                // Ignore disappearing processes or permission errors
            }
        }

        if (deltaSystem > 0) {
            this.previousSystemCpuTime = currentSystemCpu;
        }

        // Clean up previous ticks map for dead processes
        Set<Long> livePids = processes.stream().map(ProcessInfo::getPid).collect(Collectors.toSet());
        previousProcessCpuTimes.keySet().removeIf(pid -> !livePids.contains(pid));

        processes.sort(Comparator.comparingLong(ProcessInfo::getPid));
        return processes;
    }

    /**
     * Parses process details from /proc/[pid]/stat, /proc/[pid]/status, and /proc/[pid]/cmdline.
     */
    private ProcessInfo parseProcessFromProc(long pid) {
        Path statPath = Paths.get(procBasePath, String.valueOf(pid), "stat");
        Path statusPath = Paths.get(procBasePath, String.valueOf(pid), "status");
        Path cmdlinePath = Paths.get(procBasePath, String.valueOf(pid), "cmdline");

        try {
            if (!Files.exists(statPath)) {
                return null;
            }

            String statContent = Files.readString(statPath, StandardCharsets.UTF_8);
            int openParen = statContent.indexOf('(');
            int closeParen = statContent.lastIndexOf(')');
            if (openParen == -1 || closeParen == -1 || closeParen <= openParen) {
                return null;
            }

            String name = statContent.substring(openParen + 1, closeParen);
            String rest = statContent.substring(closeParen + 1).trim();
            String[] tokens = rest.split("\\s+");

            String state = (tokens.length > 0) ? tokens[0] : "?";
            long ppid = (tokens.length > 1) ? Long.parseLong(tokens[1]) : 0L;
            int threadCount = (tokens.length > 17) ? Math.max(1, Integer.parseInt(tokens[17])) : 1;
            long startTime = (tokens.length > 19) ? Long.parseLong(tokens[19]) : 0L;

            // Parse /proc/[pid]/status for VmRSS and Uid
            long memoryKb = 0L;
            String user = "unknown";
            if (Files.exists(statusPath)) {
                try {
                    List<String> statusLines = Files.readAllLines(statusPath, StandardCharsets.UTF_8);
                    for (String line : statusLines) {
                        Matcher vmRssMatcher = VMRSS_PATTERN.matcher(line);
                        if (vmRssMatcher.find()) {
                            memoryKb = Long.parseLong(vmRssMatcher.group(1));
                        }
                        Matcher threadsMatcher = THREADS_PATTERN.matcher(line);
                        if (threadsMatcher.find() && threadCount <= 1) {
                            threadCount = Integer.parseInt(threadsMatcher.group(1));
                        }
                        Matcher uidMatcher = UID_PATTERN.matcher(line);
                        if (uidMatcher.find()) {
                            String uid = uidMatcher.group(1);
                            user = ("0".equals(uid)) ? "root" : "user-" + uid;
                        }
                    }
                } catch (IOException ignored) {
                    // Ignore status read errors
                }
            }

            // Parse /proc/[pid]/cmdline
            String command = name;
            if (Files.exists(cmdlinePath)) {
                try {
                    byte[] bytes = Files.readAllBytes(cmdlinePath);
                    if (bytes.length > 0) {
                        String rawCmd = new String(bytes, StandardCharsets.UTF_8);
                        String cleanCmd = rawCmd.replace('\0', ' ').trim();
                        if (!cleanCmd.isEmpty()) {
                            command = cleanCmd;
                        }
                    }
                } catch (IOException ignored) {
                    // Fall back to name
                }
            }

            return new ProcessInfo(pid, name, ppid, state, 0.0, memoryKb, threadCount, command, user, startTime);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Reads process user time + system time (utime + stime) in clock ticks from /proc/[pid]/stat.
     */
    private long readProcessCpuTicks(long pid) {
        Path statPath = Paths.get(procBasePath, String.valueOf(pid), "stat");
        try {
            if (!Files.exists(statPath)) {
                return 0L;
            }
            String statContent = Files.readString(statPath, StandardCharsets.UTF_8);
            int closeParen = statContent.lastIndexOf(')');
            if (closeParen == -1) {
                return 0L;
            }
            String rest = statContent.substring(closeParen + 1).trim();
            String[] tokens = rest.split("\\s+");
            // utime is token index 11 (14th field in /proc/stat), stime is token index 12 (15th field)
            if (tokens.length > 12) {
                long utime = Long.parseLong(tokens[11]);
                long stime = Long.parseLong(tokens[12]);
                return utime + stime;
            }
        } catch (Exception ignored) {
            // Process terminated or inaccessible
        }
        return 0L;
    }

    /**
     * Reads total aggregate system CPU time in clock ticks from the first line of /proc/stat.
     */
    private long readSystemTotalCpuTime() {
        Path statPath = Paths.get(procBasePath, "stat");
        try {
            if (Files.exists(statPath)) {
                List<String> lines = Files.readAllLines(statPath, StandardCharsets.UTF_8);
                for (String line : lines) {
                    if (line.startsWith("cpu ")) {
                        String[] parts = line.trim().split("\\s+");
                        long total = 0L;
                        for (int i = 1; i < parts.length; i++) {
                            try {
                                total += Long.parseLong(parts[i]);
                            } catch (NumberFormatException ignored) {
                            }
                        }
                        return total;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        // Fallback calculation using system nanoTime
        return System.nanoTime() / 10_000_000L;
    }

    /**
     * Fallback implementation using Java 9+ ProcessHandle API when /proc is unavailable.
     */
    private List<ProcessInfo> getProcessListFromProcessHandle() {
        return ProcessHandle.allProcesses()
                .map(this::convertHandleToProcessInfo)
                .sorted(Comparator.comparingLong(ProcessInfo::getPid))
                .collect(Collectors.toList());
    }

    /**
     * Converts a Java ProcessHandle to a ProcessInfo record.
     */
    private ProcessInfo convertHandleToProcessInfo(ProcessHandle ph) {
        long pid = ph.pid();
        long ppid = ph.parent().map(ProcessHandle::pid).orElse(0L);
        ProcessHandle.Info info = ph.info();

        String command = info.commandLine().orElseGet(() -> info.command().orElse("process-" + pid));
        String name = info.command()
                .map(cmd -> {
                    int lastSep = Math.max(cmd.lastIndexOf('/'), cmd.lastIndexOf('\\'));
                    return (lastSep >= 0 && lastSep < cmd.length() - 1) ? cmd.substring(lastSep + 1) : cmd;
                })
                .orElse("proc-" + pid);

        String user = info.user().orElse("unknown");
        String state = ph.isAlive() ? "R" : "S";
        long startTime = info.startInstant().map(instant -> instant.toEpochMilli()).orElse(0L);

        // Approximate CPU calculation using ProcessHandle totalCpuDuration
        double cpuUsage = 0.0;
        if (info.totalCpuDuration().isPresent()) {
            long totalNanos = info.totalCpuDuration().get().toNanos();
            Long prevNanos = previousProcessCpuTimes.get(pid);
            if (prevNanos != null && totalNanos >= prevNanos) {
                long delta = totalNanos - prevNanos;
                // Roughly 100ms sample interval approximation
                cpuUsage = Math.min(100.0, (delta / 1_000_000_000.0) * 100.0);
            }
            previousProcessCpuTimes.put(pid, totalNanos);
        }

        // Default memory estimate (not directly exposed by ProcessHandle)
        long memoryKb = 1024L;

        return new ProcessInfo(pid, name, ppid, state, cpuUsage, memoryKb, 1, command, user, startTime);
    }
}
