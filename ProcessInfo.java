package com.procura.monitoring;

import java.util.Objects;

/**
 * Data model representing a snapshot of an operating system process.
 * Contains core process attributes including identifiers, execution state,
 * CPU and memory utilization, command line, and user identity.
 */
public class ProcessInfo implements Comparable<ProcessInfo> {

    private final long pid;
    private final String name;
    private final long ppid;
    private final String state;
    private double cpuUsage;
    private final long memoryKb;
    private final int threadCount;
    private final String command;
    private final String user;
    private final long startTime;

    /**
     * Constructs a new ProcessInfo record with all process details.
     *
     * @param pid         Unique Process Identifier.
     * @param name        Executable or process name.
     * @param ppid        Parent Process Identifier.
     * @param state       Current execution state (e.g., "R" (Running), "S" (Sleeping), "Z" (Zombie)).
     * @param cpuUsage    CPU utilization percentage (0.0 to 100.0+).
     * @param memoryKb    Resident memory consumption in Kilobytes.
     * @param threadCount Number of active threads in this process.
     * @param command     Full command line or command arguments.
     * @param user        Username under which the process is executing.
     * @param startTime   Process start time in milliseconds since epoch or system boot ticks.
     */
    public ProcessInfo(long pid,
                       String name,
                       long ppid,
                       String state,
                       double cpuUsage,
                       long memoryKb,
                       int threadCount,
                       String command,
                       String user,
                       long startTime) {
        this.pid = pid;
        this.name = (name != null && !name.isEmpty()) ? name : "process-" + pid;
        this.ppid = ppid;
        this.state = (state != null && !state.isEmpty()) ? state : "?";
        this.cpuUsage = Math.max(0.0, cpuUsage);
        this.memoryKb = Math.max(0L, memoryKb);
        this.threadCount = Math.max(1, threadCount);
        this.command = (command != null && !command.isEmpty()) ? command : this.name;
        this.user = (user != null && !user.isEmpty()) ? user : "unknown";
        this.startTime = startTime;
    }

    public long getPid() {
        return pid;
    }

    public String getName() {
        return name;
    }

    public long getPpid() {
        return ppid;
    }

    public String getState() {
        return state;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public void setCpuUsage(double cpuUsage) {
        this.cpuUsage = Math.max(0.0, cpuUsage);
    }

    public long getMemoryKb() {
        return memoryKb;
    }

    public int getThreadCount() {
        return threadCount;
    }

    public String getCommand() {
        return command;
    }

    public String getUser() {
        return user;
    }

    public long getStartTime() {
        return startTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProcessInfo that = (ProcessInfo) o;
        return pid == that.pid;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pid);
    }

    @Override
    public int compareTo(ProcessInfo other) {
        if (other == null) return 1;
        return Long.compare(this.pid, other.pid);
    }

    @Override
    public String toString() {
        return String.format("ProcessInfo{pid=%d, name='%s', ppid=%d, state='%s', cpuUsage=%.2f%%, memoryKb=%d KB, threads=%d, user='%s'}",
                pid, name, ppid, state, cpuUsage, memoryKb, threadCount, user);
    }
}
