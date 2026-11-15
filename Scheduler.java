package com.procura.scheduler;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for CPU scheduling algorithm implementations.
 * Provides template operations, job cloning routines, and Gantt chart aggregation helpers.
 */
public abstract class Scheduler {

    /**
     * Executes the scheduling simulation for the provided list of jobs.
     *
     * @param jobs Input list of workload jobs.
     * @return {@link SchedulingResult} containing timeline execution details and performance metrics.
     */
    public abstract SchedulingResult schedule(List<Job> jobs);

    /**
     * Returns the human-readable display name of this scheduling algorithm.
     *
     * @return Algorithm name.
     */
    public abstract String getName();

    /**
     * Creates independent, cloned copies of all jobs in the input list.
     * This ensures the simulation can run repeatedly without mutating caller data.
     *
     * @param jobs Original list of jobs.
     * @return List of newly cloned {@link Job} instances.
     */
    protected List<Job> cloneJobs(List<Job> jobs) {
        List<Job> clones = new ArrayList<>();
        if (jobs != null) {
            for (Job job : jobs) {
                if (job != null) {
                    clones.add(job.copy());
                }
            }
        }
        return clones;
    }

    /**
     * Appends a new Gantt chart execution slice, merging contiguous intervals
     * belonging to the identical job ID if applicable.
     *
     * @param ganttChart Target Gantt chart list.
     * @param jobId      Identifier of the job (or "IDLE").
     * @param startTime  Start clock tick.
     * @param endTime    End clock tick.
     */
    protected void addGanttEntry(List<SchedulingResult.GanttEntry> ganttChart, String jobId, int startTime, int endTime) {
        if (startTime >= endTime || ganttChart == null) {
            return;
        }

        if (!ganttChart.isEmpty()) {
            SchedulingResult.GanttEntry last = ganttChart.get(ganttChart.size() - 1);
            if (last.getJobId().equals(jobId) && last.getEndTime() == startTime) {
                // Coalesce contiguous interval for the same job
                ganttChart.set(ganttChart.size() - 1, new SchedulingResult.GanttEntry(jobId, last.getStartTime(), endTime));
                return;
            }
        }

        ganttChart.add(new SchedulingResult.GanttEntry(jobId, startTime, endTime));
    }
}
