package com.procura.analytics;

import com.procura.util.SystemInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

/**
 * Service that tracks system resource utilization telemetry over time,
 * coordinates data export to CSV, and orchestrates Python-based performance graph generation.
 */
public class PerformanceAnalyzer {

    private final List<double[]> cpuHistory = new CopyOnWriteArrayList<>();
    private final List<double[]> memoryHistory = new CopyOnWriteArrayList<>();

    /**
     * Records a CPU usage sample.
     *
     * @param timestamp Time offset or epoch in seconds.
     * @param usage     CPU utilization percentage [0.0, 100.0].
     */
    public void addCpuSample(double timestamp, double usage) {
        cpuHistory.add(new double[]{timestamp, usage});
    }

    /**
     * Records a Memory usage sample.
     *
     * @param timestamp Time offset or epoch in seconds.
     * @param usage     Memory utilization percentage [0.0, 100.0].
     */
    public void addMemorySample(double timestamp, double usage) {
        memoryHistory.add(new double[]{timestamp, usage});
    }

    /**
     * Returns an unmodifiable snapshot copy of the historical CPU utilization samples.
     *
     * @return List of [timestamp, usage] pairs.
     */
    public List<double[]> getCpuHistory() {
        return Collections.unmodifiableList(new ArrayList<>(cpuHistory));
    }

    /**
     * Returns an unmodifiable snapshot copy of the historical Memory utilization samples.
     *
     * @return List of [timestamp, usage] pairs.
     */
    public List<double[]> getMemoryHistory() {
        return Collections.unmodifiableList(new ArrayList<>(memoryHistory));
    }

    /**
     * Clears all recorded CPU and memory history.
     */
    public void clearHistory() {
        cpuHistory.clear();
        memoryHistory.clear();
    }

    /**
     * Exports all recorded telemetry datasets into CSV files within the specified data directory.
     *
     * @param dataDir Directory path where CSV files will be stored.
     * @throws IOException If writing files fails.
     */
    public void exportAllData(String dataDir) throws IOException {
        if (dataDir == null || dataDir.isEmpty()) {
            throw new IllegalArgumentException("Data directory path cannot be null or empty.");
        }

        File dir = new File(dataDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File cpuFile = new File(dir, "cpu_history.csv");
        CsvExporter.exportCpuHistory(getCpuHistory(), cpuFile.getAbsolutePath());

        File memFile = new File(dir, "memory_history.csv");
        CsvExporter.exportMemoryHistory(getMemoryHistory(), memFile.getAbsolutePath());
    }

    /**
     * Executes Python plotting scripts in {@code pythonDir} passing {@code dataDir} and {@code graphsDir}
     * as command-line arguments to generate visual performance charts.
     *
     * @param pythonDir Directory containing Python visualization scripts.
     * @param dataDir   Directory containing exported CSV data files.
     * @param graphsDir Directory where generated chart images will be saved.
     * @return {@code true} if Python executed successfully; {@code false} if Python is unavailable or execution failed.
     */
    public boolean runPythonPlots(String pythonDir, String dataDir, String graphsDir) {
        String pythonCmd = SystemInfo.getPythonExecutable();
        if (pythonCmd == null) {
            System.err.println("Warning: Python runtime (python3 or python) not found on PATH. Skipping plot generation.");
            return false;
        }

        File pyDir = new File(pythonDir);
        if (!pyDir.exists() || !pyDir.isDirectory()) {
            System.err.println("Warning: Python scripts directory does not exist: " + pythonDir);
            return false;
        }

        File graphDirFile = new File(graphsDir);
        if (!graphDirFile.exists()) {
            graphDirFile.mkdirs();
        }

        File[] scripts = pyDir.listFiles((dir, name) -> name.endsWith(".py"));
        if (scripts == null || scripts.length == 0) {
            System.err.println("Notice: No Python (.py) plotting scripts found in " + pythonDir);
            return false;
        }

        boolean allSucceeded = true;
        for (File script : scripts) {
            try {
                ProcessBuilder pb = new ProcessBuilder(
                        pythonCmd,
                        script.getAbsolutePath(),
                        new File(dataDir).getAbsolutePath(),
                        graphDirFile.getAbsolutePath()
                );
                pb.redirectErrorStream(true);
                Process process = pb.start();

                boolean finished = process.waitFor(20, TimeUnit.SECONDS);
                if (!finished) {
                    process.destroyForcibly();
                    System.err.println("Warning: Python script timed out: " + script.getName());
                    allSucceeded = false;
                    continue;
                }

                if (process.exitValue() != 0) {
                    System.err.println("Warning: Python script " + script.getName() + " exited with code " + process.exitValue());
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.err.println("  [py] " + line);
                        }
                    }
                    allSucceeded = false;
                }
            } catch (Exception e) {
                System.err.println("Error executing Python plotting script " + script.getName() + ": " + e.getMessage());
                allSucceeded = false;
            }
        }

        return allSucceeded;
    }
}
