package com.procura.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Utility class providing system inspection methods for OS detection,
 * Java environment details, and external Python runtime availability.
 */
public final class SystemInfo {

    private SystemInfo() {
        // Prevent instantiation of utility class
    }

    /**
     * Determines whether the current operating system is Linux.
     *
     * @return {@code true} if running on Linux; {@code false} otherwise.
     */
    public static boolean isLinux() {
        String os = getOsName().toLowerCase();
        return os.contains("linux") || os.contains("unix");
    }

    /**
     * Retrieves the operating system name.
     *
     * @return OS name string from system property {@code os.name}.
     */
    public static String getOsName() {
        return System.getProperty("os.name", "Unknown OS");
    }

    /**
     * Retrieves the operating system version.
     *
     * @return OS version string from system property {@code os.version}.
     */
    public static String getOsVersion() {
        return System.getProperty("os.version", "Unknown Version");
    }

    /**
     * Retrieves the Java runtime version.
     *
     * @return Java version string from system property {@code java.version}.
     */
    public static String getJavaVersion() {
        return System.getProperty("java.version", "Unknown Java Version");
    }

    /**
     * Determines whether a working Python runtime (python3 or python) is available on the system PATH.
     *
     * @return {@code true} if python3 or python can be invoked successfully; {@code false} otherwise.
     */
    public static boolean isPythonAvailable() {
        return getPythonExecutable() != null;
    }

    /**
     * Determines whether the Python matplotlib library is installed and importable.
     *
     * @return {@code true} if matplotlib can be imported via Python; {@code false} otherwise.
     */
    public static boolean isMatplotlibAvailable() {
        String pythonCmd = getPythonExecutable();
        if (pythonCmd == null) {
            return false;
        }

        try {
            ProcessBuilder pb = new ProcessBuilder(pythonCmd, "-c", "import matplotlib; print('ok')");
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(5, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }

            if (process.exitValue() == 0) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                    String output = reader.readLine();
                    return output != null && output.contains("ok");
                }
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Resolves the primary Python executable command available on this system.
     * Checks "python3" first, then "python".
     *
     * @return Command string ("python3" or "python"), or {@code null} if neither is available.
     */
    public static String getPythonExecutable() {
        String[] candidates = new String[]{"python3", "python"};
        for (String candidate : candidates) {
            if (checkCommand(candidate, "--version")) {
                return candidate;
            }
        }
        return null;
    }

    /**
     * Helper method to execute a command with arguments and verify successful exit (code 0) within a timeout.
     *
     * @param command Base executable command.
     * @param args    Arguments to pass.
     * @return {@code true} if command executed and exited with status 0; {@code false} otherwise.
     */
    private static boolean checkCommand(String command, String... args) {
        try {
            String[] fullCommand = new String[args.length + 1];
            fullCommand[0] = command;
            System.arraycopy(args, 0, fullCommand, 1, args.length);

            ProcessBuilder pb = new ProcessBuilder(fullCommand);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            boolean finished = process.waitFor(4, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                return false;
            }
            return process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
