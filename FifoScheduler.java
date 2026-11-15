package com.procura.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * First-Come, First-Served (FIFO / FCFS) non-preemptive CPU scheduling algorithm.
 * Jobs are dispatched in the strict order of their arrival time, breaking ties
 * using the unique Job ID.
 */
public class FifoScheduler extends Scheduler {

    @Override
    public String getName() {
        return "First-In, First-Out (FIFO)";
    }

    @Override
    public SchedulingResult schedule(List<Job> inputJobs) {
        if (inputJobs == null || inputJobs.isEmpty()) {
            return new SchedulingResult(new ArrayList<>(), new ArrayList<>(), 0);
        }

        List<Job> jobs = cloneJobs(inputJobs);
        // Sort primarily by arrival time, secondarily by jobId for deterministic order
        jobs.sort(Comparator.comparingInt(Job::getArrivalTime).thenComparing(Job::getJobId));

        List<Job> completedJobs = new ArrayList<>();
        List<SchedulingResult.GanttEntry> ganttChart = new ArrayList<>();
        int currentTime = 0;

        for (Job job : jobs) {
            // If the CPU is idle waiting for the next job arrival, advance clock
            if (currentTime < job.getArrivalTime()) {
                addGanttEntry(ganttChart, "IDLE", currentTime, job.getArrivalTime());
                currentTime = job.getArrivalTime();
            }

            int startTime = currentTime;
            job.setFirstExecutionTime(startTime);
            job.setStatus(Job.Status.RUNNING);

            int endTime = currentTime + job.getBurstTime();
            addGanttEntry(ganttChart, job.getJobId(), startTime, endTime);

            currentTime = endTime;
            job.setCompletionTime(currentTime);
            job.setRemainingTime(0);
            job.setStatus(Job.Status.COMPLETED);

            completedJobs.add(job);
        }

        return new SchedulingResult(completedJobs, ganttChart, currentTime);
    }
}
