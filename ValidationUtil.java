package com.procura.util;

/**
 * Utility class providing validation methods for job scheduling inputs and numeric parsing.
 */
public final class ValidationUtil {

    private ValidationUtil() {
        // Prevent instantiation of utility class
    }

    /**
     * Validates that a job identifier is non-null and not empty/blank.
     *
     * @param jobId The job identifier string.
     * @return {@code true} if valid; {@code false} otherwise.
     */
    public static boolean validateJobId(String jobId) {
        return jobId != null && !jobId.trim().isEmpty();
    }

    /**
     * Validates that the CPU burst time is strictly positive (> 0).
     *
     * @param burstTime The CPU burst time in simulation clock units.
     * @return {@code true} if strictly greater than 0; {@code false} otherwise.
     */
    public static boolean validateBurstTime(int burstTime) {
        return burstTime > 0;
    }

    /**
     * Validates that the arrival time is non-negative (>= 0).
     *
     * @param arrivalTime The arrival time in simulation clock units.
     * @return {@code true} if greater than or equal to 0; {@code false} otherwise.
     */
    public static boolean validateArrivalTime(int arrivalTime) {
        return arrivalTime >= 0;
    }

    /**
     * Validates that the priority value is non-negative (>= 0).
     *
     * @param priority Priority level (lower numeric value represents higher priority).
     * @return {@code true} if greater than or equal to 0; {@code false} otherwise.
     */
    public static boolean validatePriority(int priority) {
        return priority >= 0;
    }

    /**
     * Validates that the Round Robin time quantum is strictly positive (> 0).
     *
     * @param quantum Time slice quantum.
     * @return {@code true} if strictly greater than 0; {@code false} otherwise.
     */
    public static boolean validateTimeQuantum(int quantum) {
        return quantum > 0;
    }

    /**
     * Parses a string input into a strictly positive integer (> 0).
     *
     * @param text The input string representing an integer.
     * @return The parsed positive integer.
     * @throws IllegalArgumentException If text is null, invalid, or not strictly positive.
     */
    public static int parsePositiveInt(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value <= 0) {
                throw new IllegalArgumentException("Parsed value must be strictly positive (> 0), got: " + value);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer format: \"" + text + "\"", e);
        }
    }

    /**
     * Parses a string input into a non-negative integer (>= 0).
     *
     * @param text The input string representing an integer.
     * @return The parsed non-negative integer.
     * @throws IllegalArgumentException If text is null, invalid, or negative.
     */
    public static int parseNonNegativeInt(String text) {
        if (text == null || text.trim().isEmpty()) {
            throw new IllegalArgumentException("Input string cannot be null or empty.");
        }
        try {
            int value = Integer.parseInt(text.trim());
            if (value < 0) {
                throw new IllegalArgumentException("Parsed value must be non-negative (>= 0), got: " + value);
            }
            return value;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid integer format: \"" + text + "\"", e);
        }
    }
}
