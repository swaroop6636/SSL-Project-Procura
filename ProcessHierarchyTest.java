package com.procura.monitoring;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ProcessHierarchyTest {

    @Test
    @DisplayName("Verify process tree hierarchy construction and orphan parenting")
    void testHierarchy() {
        ProcessHierarchy hierarchy = new ProcessHierarchy();

        ProcessInfo init = new ProcessInfo(1, "systemd", 0, "S", 0.1, 10240, 1, "systemd", "root", 100);
        ProcessInfo bash = new ProcessInfo(100, "bash", 1, "S", 0.0, 5120, 1, "bash", "user", 200);
        ProcessInfo orphan = new ProcessInfo(200, "worker", 9999, "R", 1.2, 4096, 2, "worker", "user", 300);

        ProcessHierarchy.ProcessNode root = hierarchy.buildHierarchy(List.of(init, bash, orphan));

        assertNotNull(root);
        assertEquals(1, root.getPid());

        List<ProcessHierarchy.ProcessNode> rootChildren = hierarchy.getChildren(1);
        assertEquals(2, rootChildren.size()); // bash and orphan (parented under 1)

        ProcessHierarchy.ProcessNode bashParent = hierarchy.getParent(100);
        assertNotNull(bashParent);
        assertEquals(1, bashParent.getPid());

        ProcessHierarchy.ProcessNode orphanParent = hierarchy.getParent(200);
        assertNotNull(orphanParent);
        assertEquals(1, orphanParent.getPid()); // Orphan reparented to root
    }
}
