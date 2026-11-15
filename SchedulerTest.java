package com.procura.scheduler;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests verifying that the scheduling algorithms (FIFO, Priority, Round Robin)
 * produce mathematically exact results as defined in the project specification.
 */
public class SchedulerTest {

    private static final double EPSILON = 0.001;

    @Test
    @DisplayName("Verify FifoScheduler produces exact metrics for textbook example")
    void testFifoScheduler() {
        FifoScheduler scheduler = new FifoScheduler();

        // Jobs: J1(arrival=0, burst=5), J2(arrival=1, burst=3), J3(arrival=2, burst=4)
        List<Job> jobs = List.of(
                new Job("J1", 0, 5),
                new Job("J2", 1, 3),
                new Job("J3", 2, 4)
        );

        SchedulingResult result = scheduler.schedule(jobs);

        assertNotNull(result);
        assertEquals(3, result.getCompletedJobs().size());
        assertEquals(12, result.getTotalSimulationTime());

        Job j1 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J1")).findFirst().orElseThrow();
        assertEquals(0, j1.getFirstExecutionTime());
        assertEquals(5, j1.getCompletionTime());
        assertEquals(5, j1.getTurnaroundTime());
        assertEquals(0, j1.getWaitingTime());
        assertEquals(0, j1.getResponseTime());

        Job j2 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J2")).findFirst().orElseThrow();
        assertEquals(5, j2.getFirstExecutionTime());
        assertEquals(8, j2.getCompletionTime());
        assertEquals(7, j2.getTurnaroundTime());
        assertEquals(4, j2.getWaitingTime());
        assertEquals(4, j2.getResponseTime());

        Job j3 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J3")).findFirst().orElseThrow();
        assertEquals(8, j3.getFirstExecutionTime());
        assertEquals(12, j3.getCompletionTime());
        assertEquals(10, j3.getTurnaroundTime());
        assertEquals(6, j3.getWaitingTime());
        assertEquals(6, j3.getResponseTime());

        // Avg waiting = (0 + 4 + 6) / 3 = 3.333
        assertEquals(10.0 / 3.0, result.getAvgWaitingTime(), EPSILON);
        // Avg turnaround = (5 + 7 + 10) / 3 = 7.333
        assertEquals(22.0 / 3.0, result.getAvgTurnaroundTime(), EPSILON);
        // Avg response = (0 + 4 + 6) / 3 = 3.333
        assertEquals(10.0 / 3.0, result.getAvgResponseTime(), EPSILON);
        // CPU Util = (12 / 12) * 100 = 100%
        assertEquals(100.0, result.getCpuUtilization(), EPSILON);
        // Throughput = 3 / 12 = 0.25
        assertEquals(0.25, result.getThroughput(), EPSILON);
    }

    @Test
    @DisplayName("Verify PriorityScheduler produces exact metrics for textbook example")
    void testPriorityScheduler() {
        PriorityScheduler scheduler = new PriorityScheduler();

        // Jobs: J1(arrival=0, burst=4, priority=3), J2(arrival=1, burst=3, priority=1), J3(arrival=2, burst=5, priority=2)
        List<Job> jobs = List.of(
                new Job("J1", 0, 4, 3),
                new Job("J2", 1, 3, 1),
                new Job("J3", 2, 5, 2)
        );

        SchedulingResult result = scheduler.schedule(jobs);

        assertNotNull(result);
        assertEquals(3, result.getCompletedJobs().size());
        assertEquals(12, result.getTotalSimulationTime());

        Job j1 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J1")).findFirst().orElseThrow();
        assertEquals(0, j1.getFirstExecutionTime());
        assertEquals(4, j1.getCompletionTime());
        assertEquals(4, j1.getTurnaroundTime());
        assertEquals(0, j1.getWaitingTime());
        assertEquals(0, j1.getResponseTime());

        Job j2 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J2")).findFirst().orElseThrow();
        assertEquals(4, j2.getFirstExecutionTime());
        assertEquals(7, j2.getCompletionTime());
        assertEquals(6, j2.getTurnaroundTime());
        assertEquals(3, j2.getWaitingTime());
        assertEquals(3, j2.getResponseTime());

        Job j3 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J3")).findFirst().orElseThrow();
        assertEquals(7, j3.getFirstExecutionTime());
        assertEquals(12, j3.getCompletionTime());
        assertEquals(10, j3.getTurnaroundTime());
        assertEquals(5, j3.getWaitingTime());
        assertEquals(5, j3.getResponseTime());

        // Avg waiting = (0 + 3 + 5) / 3 = 2.667
        assertEquals(8.0 / 3.0, result.getAvgWaitingTime(), EPSILON);
        // Avg turnaround = (4 + 6 + 10) / 3 = 6.667
        assertEquals(20.0 / 3.0, result.getAvgTurnaroundTime(), EPSILON);
        // Avg response = (0 + 3 + 5) / 3 = 2.667
        assertEquals(8.0 / 3.0, result.getAvgResponseTime(), EPSILON);
    }

    @Test
    @DisplayName("Verify RoundRobinScheduler produces exact metrics for quantum=2 textbook example")
    void testRoundRobinScheduler() {
        RoundRobinScheduler scheduler = new RoundRobinScheduler(2);

        // Jobs: J1(arrival=0, burst=5), J2(arrival=1, burst=3), J3(arrival=2, burst=4)
        List<Job> jobs = List.of(
                new Job("J1", 0, 5),
                new Job("J2", 1, 3),
                new Job("J3", 2, 4)
        );

        SchedulingResult result = scheduler.schedule(jobs);

        assertNotNull(result);
        assertEquals(3, result.getCompletedJobs().size());
        assertEquals(12, result.getTotalSimulationTime());

        Job j1 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J1")).findFirst().orElseThrow();
        assertEquals(0, j1.getFirstExecutionTime());
        assertEquals(12, j1.getCompletionTime());
        assertEquals(12, j1.getTurnaroundTime());
        assertEquals(7, j1.getWaitingTime());
        assertEquals(0, j1.getResponseTime());

        Job j2 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J2")).findFirst().orElseThrow();
        assertEquals(2, j2.getFirstExecutionTime());
        assertEquals(9, j2.getCompletionTime());
        assertEquals(8, j2.getTurnaroundTime());
        assertEquals(5, j2.getWaitingTime());
        assertEquals(1, j2.getResponseTime());

        Job j3 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J3")).findFirst().orElseThrow();
        assertEquals(4, j3.getFirstExecutionTime());
        assertEquals(11, j3.getCompletionTime());
        assertEquals(9, j3.getTurnaroundTime());
        assertEquals(5, j3.getWaitingTime());
        assertEquals(2, j3.getResponseTime());

        // Avg waiting = (7 + 5 + 5) / 3 = 5.667
        assertEquals(17.0 / 3.0, result.getAvgWaitingTime(), EPSILON);
        // Avg turnaround = (12 + 8 + 9) / 3 = 9.667
        assertEquals(29.0 / 3.0, result.getAvgTurnaroundTime(), EPSILON);
        // Avg response = (0 + 1 + 2) / 3 = 1.0
        assertEquals(1.0, result.getAvgResponseTime(), EPSILON);
    }

    @Test
    @DisplayName("Verify scheduling with CPU idle periods between arrivals")
    void testSchedulerWithIdlePeriods() {
        FifoScheduler scheduler = new FifoScheduler();

        // J1 arrives at t=2, burst=3; J2 arrives at t=7, burst=2
        List<Job> jobs = List.of(
                new Job("J1", 2, 3),
                new Job("J2", 7, 2)
        );

        SchedulingResult result = scheduler.schedule(jobs);

        assertEquals(9, result.getTotalSimulationTime());
        // Sum burst = 5. Total time = 9. CPU util = (5 / 9) * 100 = 55.56%
        assertEquals((5.0 / 9.0) * 100.0, result.getCpuUtilization(), EPSILON);

        // Check that Gantt chart includes IDLE entries
        boolean hasIdle = result.getGanttChart().stream().anyMatch(g -> "IDLE".equals(g.getJobId()));
        assertTrue(hasIdle, "Gantt chart should reflect IDLE periods");
    }

    @Test
    @DisplayName("Verify Priority scheduling with equal priorities breaks ties by arrival time")
    void testPriorityWithEqualPriorities() {
        PriorityScheduler scheduler = new PriorityScheduler();

        // Both J1 and J2 have equal priority 2. J1 arrives at 0, J2 arrives at 1
        List<Job> jobs = List.of(
                new Job("J1", 0, 4, 2),
                new Job("J2", 1, 3, 2)
        );

        SchedulingResult result = scheduler.schedule(jobs);
        assertNotNull(result);
        assertEquals(2, result.getCompletedJobs().size());

        Job j1 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J1")).findFirst().orElseThrow();
        Job j2 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J2")).findFirst().orElseThrow();

        // J1 should execute first because it arrived earlier
        assertEquals(0, j1.getFirstExecutionTime());
        assertEquals(4, j1.getCompletionTime());
        assertEquals(4, j2.getFirstExecutionTime());
        assertEquals(7, j2.getCompletionTime());
    }

    @Test
    @DisplayName("Verify jobs arriving while another job is actively executing")
    void testJobsArrivingWhileAnotherJobExecutes() {
        FifoScheduler scheduler = new FifoScheduler();

        // J1 runs from t=0 to 6. J2 arrives at 2, J3 arrives at 4
        List<Job> jobs = List.of(
                new Job("J1", 0, 6),
                new Job("J2", 2, 2),
                new Job("J3", 4, 3)
        );

        SchedulingResult result = scheduler.schedule(jobs);
        assertEquals(11, result.getTotalSimulationTime());

        Job j2 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J2")).findFirst().orElseThrow();
        assertEquals(6, j2.getFirstExecutionTime());
        assertEquals(4, j2.getWaitingTime()); // 6 - 2 = 4
        assertEquals(8, j2.getCompletionTime());

        Job j3 = result.getCompletedJobs().stream().filter(j -> j.getJobId().equals("J3")).findFirst().orElseThrow();
        assertEquals(8, j3.getFirstExecutionTime());
        assertEquals(4, j3.getWaitingTime()); // 8 - 4 = 4
        assertEquals(11, j3.getCompletionTime());
    }

    @Test
    @DisplayName("Verify empty job list handling across all schedulers")
    void testEmptyJobList() {
        FifoScheduler fifo = new FifoScheduler();
        PriorityScheduler priority = new PriorityScheduler();
        RoundRobinScheduler rr = new RoundRobinScheduler(2);

        SchedulingResult resFifo = fifo.schedule(List.of());
        assertNotNull(resFifo);
        assertEquals(0, resFifo.getCompletedJobs().size());
        assertEquals(0, resFifo.getTotalSimulationTime());
        assertEquals(0.0, resFifo.getAvgWaitingTime(), EPSILON);

        SchedulingResult resPriority = priority.schedule(List.of());
        assertNotNull(resPriority);
        assertEquals(0, resPriority.getCompletedJobs().size());

        SchedulingResult resRR = rr.schedule(List.of());
        assertNotNull(resRR);
        assertEquals(0, resRR.getCompletedJobs().size());
    }

    @Test
    @DisplayName("Verify exact metric calculation formulas: Turnaround, Waiting, and Response times")
    void testMetricFormulasValidation() {
        FifoScheduler scheduler = new FifoScheduler();
        Job job = new Job("TEST", 5, 10);
        SchedulingResult result = scheduler.schedule(List.of(job));

        Job completed = result.getCompletedJobs().get(0);
        // Turnaround Time = Completion Time - Arrival Time
        assertEquals(completed.getCompletionTime() - completed.getArrivalTime(), completed.getTurnaroundTime());
        // Waiting Time = Turnaround Time - Burst Time
        assertEquals(completed.getTurnaroundTime() - completed.getBurstTime(), completed.getWaitingTime());
        // Response Time = First Execution Time - Arrival Time
        assertEquals(completed.getFirstExecutionTime() - completed.getArrivalTime(), completed.getResponseTime());
    }
}
