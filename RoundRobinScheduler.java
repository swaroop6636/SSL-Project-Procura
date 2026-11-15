package com.procura.scheduler;

import java.util.*;

/**
 * Preemptive Round Robin (RR) CPU scheduling algorithm.
 * Allocates fixed time slices (time quantum) to processes cyclically.
 * Arriving jobs are enqueued in order of arrival, and preempted jobs
 * are requeued behind newly arrived processes.
 */
public class RoundRobinScheduler extends Scheduler {

    private int timeQuantum;

    /**
     * Constructs a Round Robin scheduler with a default time quantum of 2.
     */
    public RoundRobinScheduler() {
        this(2);
    }

    /**
     * Constructs a Round Robin scheduler with the specified time quantum.
     *
     * @param timeQuantum Positive time slice duration (> 0).
     */
    public RoundRobinScheduler(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be strictly positive: " + timeQuantum);
        }
        this.timeQuantum = timeQuantum;
    }

    public int getTimeQuantum() {
        return timeQuantum;
    }

    public void setTimeQuantum(int timeQuantum) {
        if (timeQuantum <= 0) {
            throw new IllegalArgumentException("Time quantum must be strictly positive: " + timeQuantum);
        }
        this.timeQuantum = timeQuantum;
    }

    @Override
    public String getName() {
        return "Round Robin (RR, Quantum=" + timeQuantum + ")";
    }

    @Override
    public SchedulingResult schedule(List<Job> inputJobs) {
        if (inputJobs == null || inputJobs.isEmpty()) {
            return new SchedulingResult(new ArrayList<>(), new ArrayList<>(), 0);
        }

        List<Job> unarrived = cloneJobs(inputJobs);
        // Sort unarrived by arrival time, then by Job ID
        unarrived.sort(Comparator.comparingInt(Job::getArrivalTime).thenComparing(Job::getJobId));

        Queue<Job> readyQueue = new LinkedList<>();
        List<Job> completedJobs = new ArrayList<>();
        List<SchedulingResult.GanttEntry> ganttChart = new ArrayList<>();

        int currentTime = 0;

        // If the earliest job arrives after time 0, advance clock to first arrival
        if (!unarrived.isEmpty() && unarrived.get(0).getArrivalTime() > currentTime) {
            int firstArrival = unarrived.get(0).getArrivalTime();
            addGanttEntry(ganttChart, "IDLE", currentTime, firstArrival);
            currentTime = firstArrival;
        }

        // Add all jobs that arrived at or before the current start time
        Iterator<Job> arrivalIter = unarrived.iterator();
        while (arrivalIter.hasNext()) {
            Job j = arrivalIter.next();
            if (j.getArrivalTime() <= currentTime) {
                j.setStatus(Job.Status.READY);
                readyQueue.add(j);
                arrivalIter.remove();
            }
        }

        while (!readyQueue.isEmpty() || !unarrived.isEmpty()) {
            // If ready queue is empty but unarrived jobs remain, jump to next arrival
            if (readyQueue.isEmpty()) {
                int nextArrival = unarrived.get(0).getArrivalTime();
                addGanttEntry(ganttChart, "IDLE", currentTime, nextArrival);
                currentTime = nextArrival;

                arrivalIter = unarrived.iterator();
                while (arrivalIter.hasNext()) {
                    Job j = arrivalIter.next();
                    if (j.getArrivalTime() <= currentTime) {
                        j.setStatus(Job.Status.READY);
                        readyQueue.add(j);
                        arrivalIter.remove();
                    }
                }
            }

            Job currentJob = readyQueue.poll();
            if (currentJob == null) {
                continue;
            }

            // Record response time on first CPU dispatch
            if (currentJob.getFirstExecutionTime() == -1) {
                currentJob.setFirstExecutionTime(currentTime);
            }
            currentJob.setStatus(Job.Status.RUNNING);

            int slice = Math.min(timeQuantum, currentJob.getRemainingTime());
            int startTime = currentTime;
            int endTime = startTime + slice;

            addGanttEntry(ganttChart, currentJob.getJobId(), startTime, endTime);

            currentJob.setRemainingTime(currentJob.getRemainingTime() - slice);
            currentTime = endTime;

            // Check for newly arrived jobs during this time slice interval (startTime, endTime]
            List<Job> newlyArrived = new ArrayList<>();
            arrivalIter = unarrived.iterator();
            while (arrivalIter.hasNext()) {
                Job j = arrivalIter.next();
                if (j.getArrivalTime() <= currentTime) {
                    j.setStatus(Job.Status.READY);
                    newlyArrived.add(j);
                    arrivalIter.remove();
                }
            }
            // Enqueue newly arrived jobs in order of arrival, tiebreak by jobId
            newlyArrived.sort(Comparator.comparingInt(Job::getArrivalTime).thenComparing(Job::getJobId));
            readyQueue.addAll(newlyArrived);

            // Requeue current job if still unfinished, or mark as completed
            if (currentJob.getRemainingTime() > 0) {
                currentJob.setStatus(Job.Status.READY);
                readyQueue.add(currentJob);
            } else {
                currentJob.setCompletionTime(currentTime);
                currentJob.setStatus(Job.Status.COMPLETED);
                completedJobs.add(currentJob);
            }
        }

        return new SchedulingResult(completedJobs, ganttChart, currentTime);
    }
}
