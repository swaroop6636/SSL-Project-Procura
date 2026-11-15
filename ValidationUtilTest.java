package com.procura.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ValidationUtilTest {

    @Test
    @DisplayName("Verify job input validation rules")
    void testValidations() {
        assertTrue(ValidationUtil.validateJobId("J1"));
        assertFalse(ValidationUtil.validateJobId(""));
        assertFalse(ValidationUtil.validateJobId(null));
        assertFalse(ValidationUtil.validateJobId("   "));

        assertTrue(ValidationUtil.validateBurstTime(5));
        assertFalse(ValidationUtil.validateBurstTime(0));
        assertFalse(ValidationUtil.validateBurstTime(-1));

        assertTrue(ValidationUtil.validateArrivalTime(0));
        assertTrue(ValidationUtil.validateArrivalTime(10));
        assertFalse(ValidationUtil.validateArrivalTime(-1));

        assertTrue(ValidationUtil.validatePriority(0));
        assertTrue(ValidationUtil.validatePriority(5));
        assertFalse(ValidationUtil.validatePriority(-1));

        assertTrue(ValidationUtil.validateTimeQuantum(2));
        assertFalse(ValidationUtil.validateTimeQuantum(0));
        assertFalse(ValidationUtil.validateTimeQuantum(-1));
    }

    @Test
    @DisplayName("Verify parsing positive integers")
    void testParsePositiveInt() {
        assertEquals(42, ValidationUtil.parsePositiveInt("42"));
        assertEquals(1, ValidationUtil.parsePositiveInt(" 1 "));

        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt("0"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt("-5"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt("abc"));
        assertThrows(IllegalArgumentException.class, () -> ValidationUtil.parsePositiveInt(null));
    }
}
