package com.procura.scheduler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the results of a CPU scheduling simulation run, including
 * completed jobs, execution Gantt chart timeline, and aggregated performance metrics.
 */
public class SchedulingResult {

    /**
     * Represents a single contiguous execution slice in the scheduler's Gantt chart.
     */
    public static class GanttEntry {
        private final String jobId;
        private final int startTime;
        private final int endTime;

        public GanttEntry(String jobId, int startTime, int endTime) {
            this.jobId = jobId;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getJobId() {
            return jobId;
        }

        public int getStartTime() {
            return startTime;
        }

        public int getEndTime() {
            return endTime;
        }

        public int getDuration() {
            return endTime - startTime;
        }

        @Override
        public String toString() {
            return String.format("[%d-%d: %s]", startTime, endTime, jobId);
        }
    }

    private List<Job> completedJobs = new ArrayList<>();
    private List<GanttEntry> ganttChart = new ArrayList<>();
    private double avgWaitingTime;
    private double avgTurnaroundTime;
    private double avgResponseTime;
    private double cpuUtilization;
    private double throughput;
    private int totalSimulationTime;

    /**
     * Default constructor.
     */
    public SchedulingResult() {
    }

    /**
     * Constructs a complete SchedulingResult and computes performance statistics.
     *
     * @param completedJobs       List of finished jobs.
     * @param ganttChart          List of Gantt chart timeline intervals.
     * @param totalSimulationTime Total clock duration of the simulation.
     */
    public SchedulingResult(List<Job> completedJobs, List<GanttEntry> ganttChart, int totalSimulationTime) {
        this.completedJobs = (completedJobs != null) ? new ArrayList<>(completedJobs) : new ArrayList<>();
        this.ganttChart = (ganttChart != null) ? new ArrayList<>(ganttChart) : new ArrayList<>();
        this.totalSimulationTime = Math.max(0, totalSimulationTime);
        calculateStatistics();
    }

    /**
     * Computes mathematical averages and system utilization metrics from the completed job list:
     * <ul>
     *   <li>Turnaround Time = Completion Time - Arrival Time</li>
     *   <li>Waiting Time = Turnaround Time - Burst Time</li>
     *   <li>Response Time = First Execution Time - Arrival Time</li>
     *   <li>CPU Utilization (%) = (Sum of Bursts / Total Simulation Time) * 100</li>
     *   <li>Throughput = Number of Completed Jobs / Total Simulation Time</li>
     * </ul>
     */
    public void calculateStatistics() {
        if (completedJobs.isEmpty()) {
            this.avgWaitingTime = 0.0;
            this.avgTurnaroundTime = 0.0;
            this.avgResponseTime = 0.0;
            this.cpuUtilization = 0.0;
            this.throughput = 0.0;
            return;
        }

        int totalWaitingTime = 0;
        int totalTurnaroundTime = 0;
        int totalResponseTime = 0;
        int sumBurstTimes = 0;

        for (Job job : completedJobs) {
            int turnaround = job.getCompletionTime() - job.getArrivalTime();
            int waiting = turnaround - job.getBurstTime();
            int response = (job.getFirstExecutionTime() >= 0) ? (job.getFirstExecutionTime() - job.getArrivalTime()) : 0;

            job.setTurnaroundTime(turnaround);
            job.setWaitingTime(waiting);
            job.setResponseTime(response);

            totalTurnaroundTime += turnaround;
            totalWaitingTime += waiting;
            totalResponseTime += response;
            sumBurstTimes += job.getBurstTime();
        }

        int count = completedJobs.size();
        this.avgTurnaroundTime = (double) totalTurnaroundTime / count;
        this.avgWaitingTime = (double) totalWaitingTime / count;
        this.avgResponseTime = (double) totalResponseTime / count;

        if (totalSimulationTime > 0) {
            this.cpuUtilization = ((double) sumBurstTimes / totalSimulationTime) * 100.0;
            this.throughput = (double) count / totalSimulationTime;
        } else {
            this.cpuUtilization = 100.0;
            this.throughput = 0.0;
        }

        // Clamp cpuUtilization to max 100.0%
        this.cpuUtilization = Math.min(100.0, Math.max(0.0, this.cpuUtilization));
    }

    public List<Job> getCompletedJobs() {
        return Collections.unmodifiableList(completedJobs);
    }

    public void setCompletedJobs(List<Job> completedJobs) {
        this.completedJobs = (completedJobs != null) ? new ArrayList<>(completedJobs) : new ArrayList<>();
    }

    public List<GanttEntry> getGanttChart() {
        return Collections.unmodifiableList(ganttChart);
    }

    public void setGanttChart(List<GanttEntry> ganttChart) {
        this.ganttChart = (ganttChart != null) ? new ArrayList<>(ganttChart) : new ArrayList<>();
    }

    public double getAvgWaitingTime() {
        return avgWaitingTime;
    }

    public void setAvgWaitingTime(double avgWaitingTime) {
        this.avgWaitingTime = avgWaitingTime;
    }

    public double getAvgTurnaroundTime() {
        return avgTurnaroundTime;
    }

    public void setAvgTurnaroundTime(double avgTurnaroundTime) {
        this.avgTurnaroundTime = avgTurnaroundTime;
    }

    public double getAvgResponseTime() {
        return avgResponseTime;
    }

    public void setAvgResponseTime(double avgResponseTime) {
        this.avgResponseTime = avgResponseTime;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public void setCpuUtilization(double cpuUtilization) {
        this.cpuUtilization = cpuUtilization;
    }

    public double getThroughput() {
        return throughput;
    }

    public void setThroughput(double throughput) {
        this.throughput = throughput;
    }

    public int getTotalSimulationTime() {
        return totalSimulationTime;
    }

    public void setTotalSimulationTime(int totalSimulationTime) {
        this.totalSimulationTime = totalSimulationTime;
    }

    @Override
    public String toString() {
        return String.format(
                "SchedulingResult{jobs=%d, simTime=%d, avgWT=%.2f, avgTAT=%.2f, avgRT=%.2f, cpuUtil=%.2f%%, throughput=%.4f}",
                completedJobs.size(), totalSimulationTime, avgWaitingTime, avgTurnaroundTime, avgResponseTime, cpuUtilization, throughput);
    }
}
