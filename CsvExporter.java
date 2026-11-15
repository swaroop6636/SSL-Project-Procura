package com.procura.analytics;

import com.procura.monitoring.ProcessInfo;
import com.procura.scheduler.Job;
import com.procura.scheduler.SchedulingResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Utility exporter for persisting process snapshots, scheduling metrics,
 * historical performance telemetry, and algorithm comparison results into standard CSV format.
 */
public class CsvExporter {

    /**
     * Exports a snapshot list of monitored processes into a CSV file.
     * Header: PID,Name,PPID,State,CPU%,MemoryKB,Threads
     *
     * @param processes List of active processes to export.
     * @param filePath  Destination file path.
     * @throws IOException If file creation or write fails.
     */
    public static void exportProcessData(List<ProcessInfo> processes, String filePath) throws IOException {
        ensureParentDirectoryExists(filePath);
        try (BufferedWriter writer = createWriter(filePath)) {
            writer.write("PID,Name,PPID,State,CPU%,MemoryKB,Threads");
            writer.newLine();

            if (processes != null) {
                for (ProcessInfo p : processes) {
                    writer.write(String.format(Locale.US, "%d,%s,%d,%s,%.2f,%d,%d",
                            p.getPid(),
                            escapeCsv(p.getName()),
                            p.getPpid(),
                            escapeCsv(p.getState()),
                            p.getCpuUsage(),
                            p.getMemoryKb(),
                            p.getThreadCount()));
                    writer.newLine();
                }
            }
        }
    }

    /**
     * Exports scheduling simulation results including completed job metrics and overall averages.
     *
     * @param result   SchedulingResult instance containing simulation output.
     * @param filePath Destination file path.
     * @throws IOException If write fails.
     */
    public static void exportSchedulingResults(SchedulingResult result, String filePath) throws IOException {
        ensureParentDirectoryExists(filePath);
        try (BufferedWriter writer = createWriter(filePath)) {
            writer.write("JobID,ArrivalTime,BurstTime,Priority,CompletionTime,TurnaroundTime,WaitingTime,ResponseTime");
            writer.newLine();

            if (result != null && result.getCompletedJobs() != null) {
                for (Job job : result.getCompletedJobs()) {
                    writer.write(String.format(Locale.US, "%s,%d,%d,%d,%d,%d,%d,%d",
                            escapeCsv(job.getJobId()),
                            job.getArrivalTime(),
                            job.getBurstTime(),
                            job.getPriority(),
                            job.getCompletionTime(),
                            job.getTurnaroundTime(),
                            job.getWaitingTime(),
                            job.getResponseTime()));
                    writer.newLine();
                }

                // Summary footer
                writer.newLine();
                writer.write("# Metric,Value");
                writer.newLine();
                writer.write(String.format(Locale.US, "Average Waiting Time,%.2f", result.getAvgWaitingTime()));
                writer.newLine();
                writer.write(String.format(Locale.US, "Average Turnaround Time,%.2f", result.getAvgTurnaroundTime()));
                writer.newLine();
                writer.write(String.format(Locale.US, "Average Response Time,%.2f", result.getAvgResponseTime()));
                writer.newLine();
                writer.write(String.format(Locale.US, "CPU Utilization (%%),%.2f", result.getCpuUtilization()));
                writer.newLine();
                writer.write(String.format(Locale.US, "Throughput (jobs/unit),%.4f", result.getThroughput()));
                writer.newLine();
                writer.write(String.format(Locale.US, "Total Simulation Time,%d", result.getTotalSimulationTime()));
                writer.newLine();
            }
        }
    }

    /**
     * Exports time-series CPU utilization samples into CSV format.
     * Header: Timestamp,CpuUsagePercent
     *
     * @param timeAndUsage List of double pairs [timestamp, usagePercent].
     * @param filePath     Destination file path.
     * @throws IOException If write fails.
     */
    public static void exportCpuHistory(List<double[]> timeAndUsage, String filePath) throws IOException {
        ensureParentDirectoryExists(filePath);
        try (BufferedWriter writer = createWriter(filePath)) {
            writer.write("Timestamp,CpuUsagePercent");
            writer.newLine();

            if (timeAndUsage != null) {
                for (double[] sample : timeAndUsage) {
                    if (sample != null && sample.length >= 2) {
                        writer.write(String.format(Locale.US, "%.2f,%.2f", sample[0], sample[1]));
                        writer.newLine();
                    }
                }
            }
        }
    }

    /**
     * Exports time-series Memory utilization samples into CSV format.
     * Header: Timestamp,MemoryUsagePercent
     *
     * @param timeAndUsage List of double pairs [timestamp, memoryUsagePercent].
     * @param filePath     Destination file path.
     * @throws IOException If write fails.
     */
    public static void exportMemoryHistory(List<double[]> timeAndUsage, String filePath) throws IOException {
        ensureParentDirectoryExists(filePath);
        try (BufferedWriter writer = createWriter(filePath)) {
            writer.write("Timestamp,MemoryUsagePercent");
            writer.newLine();

            if (timeAndUsage != null) {
                for (double[] sample : timeAndUsage) {
                    if (sample != null && sample.length >= 2) {
                        writer.write(String.format(Locale.US, "%.2f,%.2f", sample[0], sample[1]));
                        writer.newLine();
                    }
                }
            }
        }
    }

    /**
     * Exports comparative performance metrics across different scheduling algorithms.
     * Header: Algorithm,AvgWaitingTime,AvgTurnaroundTime,AvgResponseTime,CpuUtilization,Throughput,TotalTime
     *
     * @param results  Map of algorithm name to its simulation result.
     * @param filePath Destination file path.
     * @throws IOException If write fails.
     */
    public static void exportAlgorithmComparison(Map<String, SchedulingResult> results, String filePath) throws IOException {
        ensureParentDirectoryExists(filePath);
        try (BufferedWriter writer = createWriter(filePath)) {
            writer.write("Algorithm,AvgWaitingTime,AvgTurnaroundTime,AvgResponseTime,CpuUtilization,Throughput,TotalTime");
            writer.newLine();

            if (results != null) {
                for (Map.Entry<String, SchedulingResult> entry : results.entrySet()) {
                    String algoName = entry.getKey();
                    SchedulingResult res = entry.getValue();
                    if (res != null) {
                        writer.write(String.format(Locale.US, "%s,%.2f,%.2f,%.2f,%.2f,%.4f,%d",
                                escapeCsv(algoName),
                                res.getAvgWaitingTime(),
                                res.getAvgTurnaroundTime(),
                                res.getAvgResponseTime(),
                                res.getCpuUtilization(),
                                res.getThroughput(),
                                res.getTotalSimulationTime()));
                        writer.newLine();
                    }
                }
            }
        }
    }

    /**
     * Escapes standard CSV strings containing commas, quotes, or newlines.
     */
    private static String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static void ensureParentDirectoryExists(String filePath) {
        File file = new File(filePath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }

    private static BufferedWriter createWriter(String filePath) throws IOException {
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath), StandardCharsets.UTF_8));
    }
}
