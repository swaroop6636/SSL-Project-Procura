package com.procura.scheduler;

/**
 * Utility class providing mathematical formulas and calculations for CPU scheduling metrics:
 * turnaround time, waiting time, response time, CPU utilization, and system throughput.
 */
public final class SchedulingMetrics {

    private SchedulingMetrics() {
        // Prevent instantiation of utility class
    }

    /**
     * Calculates the Turnaround Time for a job.
     * Turnaround Time = Completion Time - Arrival Time.
     *
     * @param completionTime Time at which the job completed execution (>= arrivalTime).
     * @param arrivalTime    Time at which the job arrived in the system (>= 0).
     * @return Turnaround time in clock units.
     */
    public static int calculateTurnaroundTime(int completionTime, int arrivalTime) {
        return Math.max(0, completionTime - arrivalTime);
    }

    /**
     * Calculates the Waiting Time for a job.
     * Waiting Time = Turnaround Time - Burst Time.
     *
     * @param turnaroundTime Total turnaround time (>= burstTime).
     * @param burstTime      CPU burst time required by the job (> 0).
     * @return Waiting time spent in ready queue.
     */
    public static int calculateWaitingTime(int turnaroundTime, int burstTime) {
        return Math.max(0, turnaroundTime - burstTime);
    }

    /**
     * Calculates the Response Time for a job.
     * Response Time = First Execution Time - Arrival Time.
     *
     * @param firstExecutionTime Time at which the job first received CPU time (>= arrivalTime).
     * @param arrivalTime        Time at which the job arrived (>= 0).
     * @return Response time.
     */
    public static int calculateResponseTime(int firstExecutionTime, int arrivalTime) {
        if (firstExecutionTime < 0) {
            return 0;
        }
        return Math.max(0, firstExecutionTime - arrivalTime);
    }

    /**
     * Calculates CPU Utilization as a percentage.
     * CPU Utilization (%) = (Total Busy Burst Time / Total Simulation Time) * 100.
     *
     * @param totalBurstTime      Sum of CPU burst times consumed by all executed jobs.
     * @param totalSimulationTime Total elapsed simulation clock duration.
     * @return CPU utilization percentage in range [0.0, 100.0].
     */
    public static double calculateCpuUtilization(int totalBurstTime, int totalSimulationTime) {
        if (totalSimulationTime <= 0) {
            return 0.0;
        }
        double util = ((double) totalBurstTime / totalSimulationTime) * 100.0;
        return Math.min(100.0, Math.max(0.0, util));
    }

    /**
     * Calculates system Throughput.
     * Throughput = Completed Jobs Count / Total Simulation Time.
     *
     * @param completedJobsCount  Number of successfully finished jobs.
     * @param totalSimulationTime Total simulation duration in clock units.
     * @return Throughput (jobs per unit time).
     */
    public static double calculateThroughput(int completedJobsCount, int totalSimulationTime) {
        if (totalSimulationTime <= 0) {
            return 0.0;
        }
        return (double) completedJobsCount / totalSimulationTime;
    }
}
