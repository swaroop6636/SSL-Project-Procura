package com.procura.monitoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessInfoTest {

    @Test
    @DisplayName("Verify ProcessInfo data model and getters")
    void testProcessInfoCreation() {
        ProcessInfo info = new ProcessInfo(
                1234L,
                "procura-worker",
                1000L,
                "R",
                15.5,
                20480L,
                4,
                "/usr/bin/procura-worker --run",
                "ubuntu",
                1700000000L
        );

        assertEquals(1234L, info.getPid());
        assertEquals("procura-worker", info.getName());
        assertEquals(1000L, info.getPpid());
        assertEquals("R", info.getState());
        assertEquals(15.5, info.getCpuUsage(), 0.001);
        assertEquals(20480L, info.getMemoryKb());
        assertEquals(4, info.getThreadCount());
        assertEquals("/usr/bin/procura-worker --run", info.getCommand());
        assertEquals("ubuntu", info.getUser());
        assertEquals(1700000000L, info.getStartTime());

        String str = info.toString();
        assertTrue(str.contains("1234"));
        assertTrue(str.contains("procura-worker"));
    }

    @Test
    @DisplayName("Verify ProcessInfo equality and hash code based on PID")
    void testProcessInfoEquality() {
        ProcessInfo info1 = new ProcessInfo(100L, "java", 1L, "S", 1.0, 5000L, 2, "java", "user", 0L);
        ProcessInfo info2 = new ProcessInfo(100L, "java-renamed", 1L, "R", 5.0, 8000L, 5, "java", "user", 0L);
        ProcessInfo info3 = new ProcessInfo(200L, "python", 1L, "S", 1.0, 5000L, 2, "python", "user", 0L);

        assertEquals(info1, info2);
        assertNotEquals(info1, info3);
        assertEquals(info1.hashCode(), info2.hashCode());
    }
}
