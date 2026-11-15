package com.procura.scheduler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Non-preemptive Priority CPU scheduling algorithm.
 * Selects the next job with the lowest numerical priority value (representing highest scheduling priority).
 * Resolves ties by earlier arrival time, and then alphabetically by Job ID.
 */
public class PriorityScheduler extends Scheduler {

    private static final Comparator<Job> PRIORITY_COMPARATOR = Comparator
            .comparingInt(Job::getPriority)
            .thenComparingInt(Job::getArrivalTime)
            .thenComparing(Job::getJobId);

    @Override
    public String getName() {
        return "Priority Scheduling (Non-Preemptive)";
    }

    @Override
    public SchedulingResult schedule(List<Job> inputJobs) {
        if (inputJobs == null || inputJobs.isEmpty()) {
            return new SchedulingResult(new ArrayList<>(), new ArrayList<>(), 0);
        }

        List<Job> pendingJobs = cloneJobs(inputJobs);
        List<Job> completedJobs = new ArrayList<>();
        List<SchedulingResult.GanttEntry> ganttChart = new ArrayList<>();
        int currentTime = 0;

        while (!pendingJobs.isEmpty()) {
            // Find all jobs that have arrived by currentTime
            List<Job> availableJobs = new ArrayList<>();
            for (Job j : pendingJobs) {
                if (j.getArrivalTime() <= currentTime) {
                    availableJobs.add(j);
                }
            }

            // If no jobs have arrived yet, advance clock to the earliest arriving job (IDLE interval)
            if (availableJobs.isEmpty()) {
                int nextArrival = pendingJobs.stream()
                        .mapToInt(Job::getArrivalTime)
                        .min()
                        .orElse(currentTime);

                addGanttEntry(ganttChart, "IDLE", currentTime, nextArrival);
                currentTime = nextArrival;

                for (Job j : pendingJobs) {
                    if (j.getArrivalTime() <= currentTime) {
                        availableJobs.add(j);
                    }
                }
            }

            // Select the arrived job with highest priority (lowest numeric value)
            availableJobs.sort(PRIORITY_COMPARATOR);
            Job selectedJob = availableJobs.get(0);
            pendingJobs.remove(selectedJob);

            int startTime = currentTime;
            selectedJob.setFirstExecutionTime(startTime);
            selectedJob.setStatus(Job.Status.RUNNING);

            int endTime = currentTime + selectedJob.getBurstTime();
            addGanttEntry(ganttChart, selectedJob.getJobId(), startTime, endTime);

            currentTime = endTime;
            selectedJob.setCompletionTime(currentTime);
            selectedJob.setRemainingTime(0);
            selectedJob.setStatus(Job.Status.COMPLETED);

            completedJobs.add(selectedJob);
        }

        return new SchedulingResult(completedJobs, ganttChart, currentTime);
    }
}
