package com.procura.monitoring;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Constructs and manages the hierarchical Unix process tree structure (parent-child relationships).
 * Automatically resolves orphan processes by attaching them to init / PID 1 (or PID 0).
 */
public class ProcessHierarchy {

    private final Map<Long, ProcessNode> nodeMap = new ConcurrentHashMap<>();
    private ProcessNode rootNode = null;

    /**
     * Inner class representing a node in the process hierarchy tree.
     */
    public static class ProcessNode {
        private final long pid;
        private final String name;
        private ProcessNode parent;
        private final List<ProcessNode> children = new ArrayList<>();

        public ProcessNode(long pid, String name) {
            this.pid = pid;
            this.name = (name != null && !name.isEmpty()) ? name : "process-" + pid;
        }

        public long getPid() {
            return pid;
        }

        public String getName() {
            return name;
        }

        public ProcessNode getParent() {
            return parent;
        }

        public void setParent(ProcessNode parent) {
            this.parent = parent;
        }

        public List<ProcessNode> getChildren() {
            return children;
        }

        /**
         * Adds a child node if it is not already present, setting this node as the child's parent.
         *
         * @param child The child ProcessNode to add.
         */
        public void addChild(ProcessNode child) {
            if (child != null && child != this && !children.contains(child)) {
                children.add(child);
                child.setParent(this);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ProcessNode that = (ProcessNode) o;
            return pid == that.pid;
        }

        @Override
        public int hashCode() {
            return Objects.hash(pid);
        }

        @Override
        public String toString() {
            return String.format("ProcessNode{pid=%d, name='%s', childrenCount=%d}", pid, name, children.size());
        }
    }

    /**
     * Builds the complete process tree from a list of ProcessInfo records.
     * Identifies the root (PID 0 or PID 1), parents orphaned processes under the root,
     * and returns the root node.
     *
     * @param processes List of active processes.
     * @return The root {@link ProcessNode} of the hierarchy.
     */
    public synchronized ProcessNode buildHierarchy(List<ProcessInfo> processes) {
        nodeMap.clear();

        if (processes == null || processes.isEmpty()) {
            rootNode = new ProcessNode(1L, "init");
            nodeMap.put(1L, rootNode);
            return rootNode;
        }

        // Pass 1: Instantiate unique nodes for all processes
        for (ProcessInfo proc : processes) {
            nodeMap.putIfAbsent(proc.getPid(), new ProcessNode(proc.getPid(), proc.getName()));
        }

        // Determine or create the root process
        if (nodeMap.containsKey(0L)) {
            rootNode = nodeMap.get(0L);
        } else if (nodeMap.containsKey(1L)) {
            rootNode = nodeMap.get(1L);
        } else {
            rootNode = new ProcessNode(1L, "init");
            nodeMap.put(1L, rootNode);
        }

        // Pass 2: Establish parent-child links
        for (ProcessInfo proc : processes) {
            long pid = proc.getPid();
            long ppid = proc.getPpid();
            ProcessNode currentNode = nodeMap.get(pid);

            if (currentNode == null || currentNode == rootNode) {
                continue;
            }

            ProcessNode parentNode = null;
            if (ppid > 0 && ppid != pid && nodeMap.containsKey(ppid)) {
                parentNode = nodeMap.get(ppid);
            }

            // Orphan process handling: parent under root node
            if (parentNode == null) {
                parentNode = rootNode;
            }

            parentNode.addChild(currentNode);
        }

        return rootNode;
    }

    /**
     * Retrieves the direct child processes of the specified process ID.
     *
     * @param pid Process ID.
     * @return Unmodifiable list of child {@link ProcessNode} instances.
     */
    public List<ProcessNode> getChildren(long pid) {
        ProcessNode node = nodeMap.get(pid);
        if (node != null) {
            return Collections.unmodifiableList(node.getChildren());
        }
        return Collections.emptyList();
    }

    /**
     * Retrieves the parent process node of the specified process ID.
     *
     * @param pid Process ID.
     * @return Parent {@link ProcessNode}, or {@code null} if it is root or not found.
     */
    public ProcessNode getParent(long pid) {
        ProcessNode node = nodeMap.get(pid);
        if (node != null) {
            return node.getParent();
        }
        return null;
    }

    /**
     * Returns the current root node of the hierarchy.
     *
     * @return Root {@link ProcessNode}.
     */
    public ProcessNode getRootNode() {
        return rootNode;
    }
}
