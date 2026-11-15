package com.procura.scheduler;

import java.util.Objects;

/**
 * Data class representing a workload job in CPU scheduling simulations.
 * Tracks job arrival, required CPU burst time, priority, execution progress,
 * and standard performance metrics (completion, turnaround, waiting, and response times).
 */
public class Job implements Comparable<Job> {

    /**
     * Lifecycle execution state of a job within the scheduler.
     */
    public enum Status {
        WAITING,
        READY,
        RUNNING,
        COMPLETED
    }

    private final String jobId;
    private final int arrivalTime;
    private final int burstTime;
    private final int priority;

    private int remainingTime;
    private int completionTime;
    private int turnaroundTime;
    private int waitingTime;
    private int responseTime;
    private int firstExecutionTime = -1;
    private Status status = Status.WAITING;

    /**
     * Constructs a Job with an explicit priority.
     *
     * @param jobId       Unique job identifier (e.g. "J1", "J2").
     * @param arrivalTime Time at which the job arrives in the system (>= 0).
     * @param burstTime   Total CPU execution time required (> 0).
     * @param priority    Priority level (lower numerical values denote higher scheduling priority).
     */
    public Job(String jobId, int arrivalTime, int burstTime, int priority) {
        if (jobId == null || jobId.trim().isEmpty()) {
            throw new IllegalArgumentException("Job ID cannot be null or empty.");
        }
        if (arrivalTime < 0) {
            throw new IllegalArgumentException("Arrival time must be non-negative: " + arrivalTime);
        }
        if (burstTime <= 0) {
            throw new IllegalArgumentException("Burst time must be strictly positive: " + burstTime);
        }
        this.jobId = jobId.trim();
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.status = Status.WAITING;
    }

    /**
     * Convenience constructor defaulting priority to 0.
     *
     * @param jobId       Unique job identifier.
     * @param arrivalTime Arrival time (>= 0).
     * @param burstTime   Burst time (> 0).
     */
    public Job(String jobId, int arrivalTime, int burstTime) {
        this(jobId, arrivalTime, burstTime, 0);
    }

    /**
     * Creates an independent copy of this job with its initial burst, arrival, and priority values
     * reset for a new simulation run.
     *
     * @return A fresh {@link Job} copy.
     */
    public Job copy() {
        Job clone = new Job(this.jobId, this.arrivalTime, this.burstTime, this.priority);
        clone.remainingTime = this.burstTime;
        clone.firstExecutionTime = -1;
        clone.status = Status.WAITING;
        clone.completionTime = 0;
        clone.turnaroundTime = 0;
        clone.waitingTime = 0;
        clone.responseTime = 0;
        return clone;
    }

    public String getJobId() {
        return jobId;
    }

    public int getArrivalTime() {
        return arrivalTime;
    }

    public int getBurstTime() {
        return burstTime;
    }

    public int getPriority() {
        return priority;
    }

    public int getRemainingTime() {
        return remainingTime;
    }

    public void setRemainingTime(int remainingTime) {
        this.remainingTime = Math.max(0, remainingTime);
    }

    public int getCompletionTime() {
        return completionTime;
    }

    public void setCompletionTime(int completionTime) {
        this.completionTime = completionTime;
    }

    public int getTurnaroundTime() {
        return turnaroundTime;
    }

    public void setTurnaroundTime(int turnaroundTime) {
        this.turnaroundTime = turnaroundTime;
    }

    public int getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(int waitingTime) {
        this.waitingTime = waitingTime;
    }

    public int getResponseTime() {
        return responseTime;
    }

    public void setResponseTime(int responseTime) {
        this.responseTime = responseTime;
    }

    public int getFirstExecutionTime() {
        return firstExecutionTime;
    }

    public void setFirstExecutionTime(int firstExecutionTime) {
        this.firstExecutionTime = firstExecutionTime;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = (status != null) ? status : Status.WAITING;
    }

    @Override
    public int compareTo(Job other) {
        if (other == null) return 1;
        int cmp = Integer.compare(this.arrivalTime, other.arrivalTime);
        if (cmp != 0) return cmp;
        return this.jobId.compareTo(other.jobId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Job job = (Job) o;
        return Objects.equals(jobId, job.jobId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(jobId);
    }

    @Override
    public String toString() {
        return String.format("Job{id='%s', arr=%d, burst=%d, pri=%d, rem=%d, comp=%d, tat=%d, wt=%d, resp=%d, status=%s}",
                jobId, arrivalTime, burstTime, priority, remainingTime, completionTime, turnaroundTime, waitingTime, responseTime, status);
    }
}
