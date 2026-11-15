package com.procura.ui;

import com.procura.monitoring.ProcessHierarchy.ProcessNode;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.tree.*;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Enumeration;

/**
 * HierarchyPanel: Visual tree representation of the Linux process hierarchy (PPID -> PID tree).
 */
public class HierarchyPanel extends JPanel {

    private final JTree processTree;
    private final DefaultTreeModel treeModel;
    private final DefaultMutableTreeNode rootTreeNode;

    private final JTextField searchField;
    private final JCheckBox autoRefreshCheckBox;
    private final JLabel nodeCountLabel;

    // Side detail labels
    private final JLabel detailPidLabel;
    private final JLabel detailNameLabel;
    private final JLabel detailParentLabel;
    private final JLabel detailChildrenCountLabel;
    private final JLabel detailSubtreeCountLabel;
    private final JTextArea childrenListArea;

    private Runnable manualRefreshAction;
    private ProcessNode currentRootNode;
    private ProcessNode selectedNode;

    public HierarchyPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. Header & Toolbar
        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        topContainer.setBackground(UITheme.BG_DARK);

        JLabel titleLabel = UITheme.createHeaderLabel("Process Hierarchy Tree");
        topContainer.add(titleLabel, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setBackground(UITheme.PANEL_BG);
        toolbar.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1), new EmptyBorder(6, 10, 6, 10)));

        JLabel searchLbl = new JLabel("Search Tree:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_MAIN);
        toolbar.add(searchLbl);

        searchField = UITheme.createTextField(14);
        searchField.setToolTipText("Enter PID or process name");
        searchField.addActionListener(e -> findInTree());
        toolbar.add(searchField);

        JButton findBtn = UITheme.createSecondaryButton("Find");
        findBtn.addActionListener(e -> findInTree());
        toolbar.add(findBtn);

        JButton expandAllBtn = UITheme.createSecondaryButton("Expand All");
        expandAllBtn.addActionListener(e -> expandAllNodes(processTree, 0, processTree.getRowCount()));
        toolbar.add(expandAllBtn);

        JButton collapseAllBtn = UITheme.createSecondaryButton("Collapse All");
        collapseAllBtn.addActionListener(e -> collapseAllNodes());
        toolbar.add(collapseAllBtn);

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh Tree");
        refreshBtn.addActionListener(e -> {
            if (manualRefreshAction != null) {
                manualRefreshAction.run();
            }
        });
        toolbar.add(refreshBtn);

        autoRefreshCheckBox = new JCheckBox("Auto-Refresh (3s)", true);
        autoRefreshCheckBox.setFont(UITheme.FONT_BODY);
        autoRefreshCheckBox.setBackground(UITheme.PANEL_BG);
        autoRefreshCheckBox.setForeground(UITheme.TEXT_MAIN);
        autoRefreshCheckBox.setFocusPainted(false);
        toolbar.add(autoRefreshCheckBox);

        nodeCountLabel = new JLabel("Nodes: 0");
        nodeCountLabel.setFont(UITheme.FONT_BOLD);
        nodeCountLabel.setForeground(UITheme.ACCENT_PRIMARY);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(nodeCountLabel);

        topContainer.add(toolbar, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // 2. Tree and Side Panel Split
        rootTreeNode = new DefaultMutableTreeNode("Root (PID 0)");
        treeModel = new DefaultTreeModel(rootTreeNode);

        processTree = new JTree(treeModel);
        processTree.setBackground(UITheme.PANEL_BG);
        processTree.setForeground(UITheme.TEXT_MAIN);
        processTree.setFont(UITheme.FONT_BODY);
        processTree.setRowHeight(24);
        processTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        processTree.setCellRenderer(new DarkTreeCellRenderer());

        JScrollPane treeScroll = new JScrollPane(processTree);
        UITheme.styleScrollPane(treeScroll);

        // Side Detail Panel
        JPanel sidePanel = new JPanel(new BorderLayout(0, 10));
        sidePanel.setPreferredSize(new Dimension(340, 0));
        sidePanel.setBackground(UITheme.CARD_BG);
        sidePanel.setBorder(UITheme.createCardBorder());

        JPanel sideHeader = new JPanel(new BorderLayout());
        sideHeader.setBackground(UITheme.CARD_BG);
        JLabel sideTitle = new JLabel("Node Information");
        sideTitle.setFont(UITheme.FONT_SUBHEADER);
        sideTitle.setForeground(UITheme.TEXT_MAIN);
        sideHeader.add(sideTitle, BorderLayout.WEST);

        JButton copyNodeBtn = UITheme.createSecondaryButton("Copy Info");
        copyNodeBtn.addActionListener(e -> {
            if (selectedNode != null) {
                String str = String.format("PID: %d\nName: %s\nParent: %s\nChildren: %d",
                        selectedNode.pid, selectedNode.name,
                        selectedNode.parent != null ? "[" + selectedNode.parent.pid + "] " + selectedNode.parent.name : "None",
                        selectedNode.children != null ? selectedNode.children.size() : 0);
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(str), null);
                JOptionPane.showMessageDialog(this, "Process node info copied!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });
        sideHeader.add(copyNodeBtn, BorderLayout.EAST);
        sidePanel.add(sideHeader, BorderLayout.NORTH);

        JPanel detailsGrid = new JPanel(new GridLayout(5, 1, 6, 6));
        detailsGrid.setBackground(UITheme.CARD_BG);

        detailPidLabel = createDetailVal("-");
        detailNameLabel = createDetailVal("-");
        detailParentLabel = createDetailVal("-");
        detailChildrenCountLabel = createDetailVal("-");
        detailSubtreeCountLabel = createDetailVal("-");

        detailsGrid.add(createFieldRow("Process ID (PID):", detailPidLabel));
        detailsGrid.add(createFieldRow("Process Name:", detailNameLabel));
        detailsGrid.add(createFieldRow("Parent Process:", detailParentLabel));
        detailsGrid.add(createFieldRow("Direct Children:", detailChildrenCountLabel));
        detailsGrid.add(createFieldRow("Subtree Total Nodes:", detailSubtreeCountLabel));
        sidePanel.add(detailsGrid, BorderLayout.CENTER);

        // Children preview list at bottom of side panel
        JPanel childrenBox = new JPanel(new BorderLayout(0, 4));
        childrenBox.setBackground(UITheme.CARD_BG);
        JLabel childrenHeader = new JLabel("Child Processes:");
        childrenHeader.setFont(UITheme.FONT_BOLD);
        childrenHeader.setForeground(UITheme.TEXT_MUTED);
        childrenBox.add(childrenHeader, BorderLayout.NORTH);

        childrenListArea = new JTextArea();
        childrenListArea.setEditable(false);
        childrenListArea.setBackground(UITheme.PANEL_BG);
        childrenListArea.setForeground(UITheme.TEXT_MAIN);
        childrenListArea.setFont(UITheme.FONT_MONO);
        childrenListArea.setRows(8);

        JScrollPane childScroll = new JScrollPane(childrenListArea);
        UITheme.styleScrollPane(childScroll);
        childrenBox.add(childScroll, BorderLayout.CENTER);
        sidePanel.add(childrenBox, BorderLayout.SOUTH);

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, treeScroll, sidePanel);
        splitPane.setResizeWeight(0.72);
        splitPane.setBackground(UITheme.BG_DARK);
        splitPane.setBorder(null);
        splitPane.setDividerSize(6);

        add(splitPane, BorderLayout.CENTER);

        // Tree selection listener
        processTree.addTreeSelectionListener(e -> {
            DefaultMutableTreeNode selected = (DefaultMutableTreeNode) processTree.getLastSelectedPathComponent();
            if (selected != null && selected.getUserObject() instanceof NodeWrapper) {
                NodeWrapper wrapper = (NodeWrapper) selected.getUserObject();
                showNodeDetails(wrapper.node);
            }
        });
    }

    private JPanel createFieldRow(String label, JLabel val) {
        JPanel row = new JPanel(new BorderLayout(6, 0));
        row.setBackground(UITheme.CARD_BG);
        JLabel lbl = new JLabel(label);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);
        row.add(lbl, BorderLayout.WEST);
        row.add(val, BorderLayout.CENTER);
        return row;
    }

    private JLabel createDetailVal(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BODY);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    private void showNodeDetails(ProcessNode node) {
        this.selectedNode = node;
        detailPidLabel.setText(String.valueOf(node.pid));
        detailNameLabel.setText(node.name);

        if (node.parent != null) {
            detailParentLabel.setText(String.format("[%d] %s", node.parent.pid, node.parent.name));
        } else {
            detailParentLabel.setText("Root / System");
        }

        int directCount = node.children != null ? node.children.size() : 0;
        detailChildrenCountLabel.setText(String.valueOf(directCount));
        detailSubtreeCountLabel.setText(String.valueOf(countSubtree(node)));

        StringBuilder sb = new StringBuilder();
        if (node.children != null && !node.children.isEmpty()) {
            for (ProcessNode c : node.children) {
                sb.append(String.format("  • [%d] %s (sub-children: %d)\n", c.pid, c.name, c.children != null ? c.children.size() : 0));
            }
        } else {
            sb.append("  (No child processes)");
        }
        childrenListArea.setText(sb.toString());
        childrenListArea.setCaretPosition(0);
    }

    private int countSubtree(ProcessNode node) {
        if (node == null) return 0;
        int count = 1;
        if (node.children != null) {
            for (ProcessNode c : node.children) {
                count += countSubtree(c);
            }
        }
        return count;
    }

    /**
     * Updates hierarchy tree from root ProcessNode.
     */
    public void updateHierarchy(ProcessNode rootNode) {
        SwingUtilities.invokeLater(() -> {
            if (rootNode == null) return;
            this.currentRootNode = rootNode;

            rootTreeNode.removeAllChildren();
            rootTreeNode.setUserObject(new NodeWrapper(rootNode));

            if (rootNode.children != null) {
                for (ProcessNode child : rootNode.children) {
                    addTreeNode(rootTreeNode, child);
                }
            }

            treeModel.reload();

            // Expand first 2 levels
            expandTreeLevel(new TreePath(rootTreeNode.getPath()), 2);

            int totalNodes = countSubtree(rootNode);
            nodeCountLabel.setText("Nodes: " + totalNodes);

            if (selectedNode != null) {
                findAndSelectPid(selectedNode.pid);
            }
        });
    }

    private void addTreeNode(DefaultMutableTreeNode parentNode, ProcessNode pNode) {
        DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(new NodeWrapper(pNode));
        parentNode.add(treeNode);
        if (pNode.children != null) {
            for (ProcessNode child : pNode.children) {
                addTreeNode(treeNode, child);
            }
        }
    }

    private void expandTreeLevel(TreePath parent, int level) {
        if (level <= 0) return;
        processTree.expandPath(parent);
        DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getLastPathComponent();
        Enumeration<?> e = node.children();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) e.nextElement();
            expandTreeLevel(parent.pathByAddingChild(child), level - 1);
        }
    }

    private void expandAllNodes(JTree tree, int startingIndex, int rowCount) {
        for (int i = startingIndex; i < rowCount; ++i) {
            tree.expandRow(i);
        }
        if (tree.getRowCount() != rowCount) {
            expandAllNodes(tree, rowCount, tree.getRowCount());
        }
    }

    private void collapseAllNodes() {
        for (int i = processTree.getRowCount() - 1; i > 0; i--) {
            processTree.collapseRow(i);
        }
    }

    private void findInTree() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) return;

        Enumeration<?> e = rootTreeNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject() instanceof NodeWrapper) {
                ProcessNode p = ((NodeWrapper) node.getUserObject()).node;
                if (String.valueOf(p.pid).equals(query) || (p.name != null && p.name.toLowerCase().contains(query))) {
                    TreePath path = new TreePath(node.getPath());
                    processTree.setSelectionPath(path);
                    processTree.scrollPathToVisible(path);
                    showNodeDetails(p);
                    return;
                }
            }
        }
        JOptionPane.showMessageDialog(this, "No matching process found for: " + query, "Not Found", JOptionPane.INFORMATION_MESSAGE);
    }

    private void findAndSelectPid(long targetPid) {
        Enumeration<?> e = rootTreeNode.breadthFirstEnumeration();
        while (e.hasMoreElements()) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) e.nextElement();
            if (node.getUserObject() instanceof NodeWrapper) {
                ProcessNode p = ((NodeWrapper) node.getUserObject()).node;
                if (p.pid == targetPid) {
                    TreePath path = new TreePath(node.getPath());
                    processTree.setSelectionPath(path);
                    return;
                }
            }
        }
    }

    public boolean isAutoRefreshEnabled() {
        return autoRefreshCheckBox.isSelected();
    }

    public void setManualRefreshAction(Runnable action) {
        this.manualRefreshAction = action;
    }

    // Helper wrapper class for tree user object
    private static class NodeWrapper {
        final ProcessNode node;
        NodeWrapper(ProcessNode node) { this.node = node; }
        @Override
        public String toString() {
            return String.format("[%d] %s", node.pid, node.name != null ? node.name : "process");
        }
    }

    // Custom dark tree cell renderer with custom circle icons
    private static class DarkTreeCellRenderer extends DefaultTreeCellRenderer {
        private final Icon greenCircleIcon = new CircleIcon(new Color(76, 175, 80));
        private final Icon grayCircleIcon = new CircleIcon(new Color(140, 150, 160));

        public DarkTreeCellRenderer() {
            setBackgroundNonSelectionColor(UITheme.PANEL_BG);
            setBackgroundSelectionColor(new Color(50, 80, 130));
            setTextNonSelectionColor(UITheme.TEXT_MAIN);
            setTextSelectionColor(Color.WHITE);
            setBorderSelectionColor(UITheme.ACCENT_PRIMARY);
            setFont(UITheme.FONT_BODY);
        }

        @Override
        public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean exp, boolean leaf, int row, boolean hasFocus) {
            super.getTreeCellRendererComponent(tree, value, sel, exp, leaf, row, hasFocus);

            if (value instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
                if (treeNode.getUserObject() instanceof NodeWrapper) {
                    ProcessNode pNode = ((NodeWrapper) treeNode.getUserObject()).node;
                    // Running / high priority processes green circle, system or sleep gray circle
                    if (pNode.pid > 2 && (pNode.children != null && !pNode.children.isEmpty())) {
                        setIcon(greenCircleIcon);
                    } else {
                        setIcon(grayCircleIcon);
                    }
                }
            }
            return this;
        }
    }

    // Custom circle icon
    private static class CircleIcon implements Icon {
        private final Color color;
        private static final int SIZE = 12;

        public CircleIcon(Color color) {
            this.color = color;
        }

        @Override
        public void paintIcon(Component c, Graphics g, int x, int y) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fillOval(x + 1, y + 2, SIZE - 2, SIZE - 2);
            g2.setColor(color.darker());
            g2.drawOval(x + 1, y + 2, SIZE - 2, SIZE - 2);
            g2.dispose();
        }

        @Override
        public int getIconWidth() { return SIZE; }
        @Override
        public int getIconHeight() { return SIZE; }
    }
}
