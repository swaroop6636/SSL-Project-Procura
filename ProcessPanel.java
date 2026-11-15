package com.procura.ui;

import com.procura.monitoring.ProcessInfo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

/**
 * ProcessPanel: Monitored Linux processes table, search/filter toolbar, and detailed view.
 */
public class ProcessPanel extends JPanel {

    private final JTextField searchField;
    private final JRadioButton searchByPidRadio;
    private final JRadioButton searchByNameRadio;
    private final JCheckBox autoRefreshCheckBox;
    private final JLabel processCountLabel;

    private final JTable processTable;
    private final DefaultTableModel tableModel;
    private final TableRowSorter<DefaultTableModel> rowSorter;

    // Process detail labels
    private final JLabel detailPidLabel;
    private final JLabel detailNameLabel;
    private final JLabel detailPpidLabel;
    private final JLabel detailStateLabel;
    private final JLabel detailUserLabel;
    private final JLabel detailCpuLabel;
    private final JLabel detailMemoryLabel;
    private final JLabel detailThreadsLabel;
    private final JLabel detailStartLabel;
    private final JTextField detailCommandField;

    private final Map<Long, ProcessInfo> processMap = new HashMap<>();
    private List<ProcessInfo> allProcesses = new ArrayList<>();
    private Runnable manualRefreshAction;
    private ProcessInfo selectedProcess;

    private static final String[] COLUMN_NAMES = {
            "PID", "Name", "PPID", "State", "CPU%", "Memory (KB)", "Threads"
    };

    public ProcessPanel() {
        setLayout(new BorderLayout(10, 10));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. Header & Toolbar
        JPanel topContainer = new JPanel(new BorderLayout(0, 8));
        topContainer.setBackground(UITheme.BG_DARK);

        JLabel titleLabel = UITheme.createHeaderLabel("Linux Process Monitor");
        topContainer.add(titleLabel, BorderLayout.NORTH);

        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        toolbar.setBackground(UITheme.PANEL_BG);
        toolbar.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1), new EmptyBorder(6, 10, 6, 10)));

        JLabel searchLbl = new JLabel("Filter:");
        searchLbl.setFont(UITheme.FONT_BOLD);
        searchLbl.setForeground(UITheme.TEXT_MAIN);
        toolbar.add(searchLbl);

        searchField = UITheme.createTextField(14);
        searchField.setToolTipText("Enter PID or process name to filter");
        toolbar.add(searchField);

        searchByNameRadio = new JRadioButton("By Name", true);
        searchByNameRadio.setFont(UITheme.FONT_BODY);
        searchByNameRadio.setBackground(UITheme.PANEL_BG);
        searchByNameRadio.setForeground(UITheme.TEXT_MAIN);
        searchByNameRadio.setFocusPainted(false);

        searchByPidRadio = new JRadioButton("By PID", false);
        searchByPidRadio.setFont(UITheme.FONT_BODY);
        searchByPidRadio.setBackground(UITheme.PANEL_BG);
        searchByPidRadio.setForeground(UITheme.TEXT_MAIN);
        searchByPidRadio.setFocusPainted(false);

        ButtonGroup searchGroup = new ButtonGroup();
        searchGroup.add(searchByNameRadio);
        searchGroup.add(searchByPidRadio);

        toolbar.add(searchByNameRadio);
        toolbar.add(searchByPidRadio);

        JButton refreshBtn = UITheme.createPrimaryButton("Refresh Now");
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

        processCountLabel = new JLabel("Processes: 0");
        processCountLabel.setFont(UITheme.FONT_BOLD);
        processCountLabel.setForeground(UITheme.ACCENT_PRIMARY);
        toolbar.add(Box.createHorizontalStrut(10));
        toolbar.add(processCountLabel);

        topContainer.add(toolbar, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // 2. Table in Center
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 0: // PID
                    case 2: // PPID
                    case 5: // Memory
                        return Long.class;
                    case 4: // CPU%
                        return Double.class;
                    case 6: // Threads
                        return Integer.class;
                    default:
                        return String.class;
                }
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        processTable = new JTable(tableModel);
        UITheme.styleTable(processTable);

        rowSorter = new TableRowSorter<>(tableModel);
        processTable.setRowSorter(rowSorter);
        processTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Custom Cell Renderers for alternating rows and colored CPU%
        DefaultTableCellRenderer standardRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UITheme.PANEL_BG : UITheme.TABLE_ROW_ALT);
                    setForeground(UITheme.TEXT_MAIN);
                } else {
                    setBackground(new Color(50, 80, 130));
                    setForeground(Color.WHITE);
                }
                if (col == 0 || col == 2 || col == 6) {
                    setHorizontalAlignment(CENTER);
                } else if (col == 5) {
                    setHorizontalAlignment(RIGHT);
                    if (value instanceof Number) {
                        setText(String.format("%,d KB", ((Number) value).longValue()));
                    }
                } else {
                    setHorizontalAlignment(LEFT);
                }
                setBorder(new EmptyBorder(0, 6, 0, 6));
                return this;
            }
        };

        DefaultTableCellRenderer cpuRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                setBorder(new EmptyBorder(0, 6, 0, 6));

                double cpu = 0.0;
                if (value instanceof Number) {
                    cpu = ((Number) value).doubleValue();
                }
                setText(String.format("%.1f%%", cpu));

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UITheme.PANEL_BG : UITheme.TABLE_ROW_ALT);
                    if (cpu >= 70.0) {
                        setForeground(UITheme.ERROR);
                        setFont(UITheme.FONT_BOLD);
                    } else if (cpu >= 30.0) {
                        setForeground(UITheme.WARNING);
                        setFont(UITheme.FONT_BOLD);
                    } else {
                        setForeground(UITheme.SUCCESS);
                        setFont(UITheme.FONT_BODY);
                    }
                } else {
                    setBackground(new Color(50, 80, 130));
                    setForeground(Color.WHITE);
                }
                return this;
            }
        };

        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            if (i == 4) {
                processTable.getColumnModel().getColumn(i).setCellRenderer(cpuRenderer);
            } else {
                processTable.getColumnModel().getColumn(i).setCellRenderer(standardRenderer);
            }
        }

        // Table Selection listener
        processTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int viewRow = processTable.getSelectedRow();
                if (viewRow >= 0) {
                    int modelRow = processTable.convertRowIndexToModel(viewRow);
                    Object pidObj = tableModel.getValueAt(modelRow, 0);
                    if (pidObj instanceof Long) {
                        long pid = (Long) pidObj;
                        ProcessInfo info = processMap.get(pid);
                        if (info != null) {
                            showProcessDetails(info);
                        }
                    }
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(processTable);
        UITheme.styleScrollPane(scrollPane);
        add(scrollPane, BorderLayout.CENTER);

        // 3. Process Detail Panel at Bottom
        JPanel detailContainer = new JPanel(new BorderLayout(0, 8));
        detailContainer.setBackground(UITheme.CARD_BG);
        detailContainer.setBorder(UITheme.createCardBorder());

        JPanel detailHeader = new JPanel(new BorderLayout());
        detailHeader.setBackground(UITheme.CARD_BG);
        JLabel detailTitle = new JLabel("Selected Process Information");
        detailTitle.setFont(UITheme.FONT_SUBHEADER);
        detailTitle.setForeground(UITheme.TEXT_MAIN);

        JPanel detailActionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        detailActionBtns.setBackground(UITheme.CARD_BG);

        JButton copyCmdBtn = UITheme.createSecondaryButton("Copy Command");
        copyCmdBtn.addActionListener(e -> {
            if (selectedProcess != null && selectedProcess.getCommand() != null) {
                Toolkit.getDefaultToolkit().getSystemClipboard()
                        .setContents(new StringSelection(selectedProcess.getCommand()), null);
                JOptionPane.showMessageDialog(this, "Command copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        JButton copyInfoBtn = UITheme.createSecondaryButton("Copy Details");
        copyInfoBtn.addActionListener(e -> {
            if (selectedProcess != null) {
                String info = String.format("PID: %d\nName: %s\nPPID: %d\nUser: %s\nState: %s\nCPU: %.1f%%\nMemory: %,d KB\nThreads: %d\nCommand: %s",
                        selectedProcess.getPid(), selectedProcess.getName(), selectedProcess.getPpid(),
                        selectedProcess.getUser(), selectedProcess.getState(), selectedProcess.getCpuUsage(),
                        selectedProcess.getMemoryKb(), selectedProcess.getThreadCount(), selectedProcess.getCommand());
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(info), null);
                JOptionPane.showMessageDialog(this, "Process details copied to clipboard!", "Copied", JOptionPane.INFORMATION_MESSAGE);
            }
        });

        detailActionBtns.add(copyCmdBtn);
        detailActionBtns.add(copyInfoBtn);

        detailHeader.add(detailTitle, BorderLayout.WEST);
        detailHeader.add(detailActionBtns, BorderLayout.EAST);
        detailContainer.add(detailHeader, BorderLayout.NORTH);

        // Grid for details
        JPanel grid = new JPanel(new GridLayout(3, 3, 10, 6));
        grid.setBackground(UITheme.CARD_BG);

        detailPidLabel = createDetailValueLabel("-");
        detailNameLabel = createDetailValueLabel("-");
        detailPpidLabel = createDetailValueLabel("-");
        detailStateLabel = createDetailValueLabel("-");
        detailUserLabel = createDetailValueLabel("-");
        detailCpuLabel = createDetailValueLabel("-");
        detailMemoryLabel = createDetailValueLabel("-");
        detailThreadsLabel = createDetailValueLabel("-");
        detailStartLabel = createDetailValueLabel("-");

        grid.add(createFieldBox("PID:", detailPidLabel));
        grid.add(createFieldBox("Name:", detailNameLabel));
        grid.add(createFieldBox("PPID:", detailPpidLabel));
        grid.add(createFieldBox("State:", detailStateLabel));
        grid.add(createFieldBox("User:", detailUserLabel));
        grid.add(createFieldBox("CPU Load:", detailCpuLabel));
        grid.add(createFieldBox("Memory Usage:", detailMemoryLabel));
        grid.add(createFieldBox("Threads:", detailThreadsLabel));
        grid.add(createFieldBox("Start Time:", detailStartLabel));

        detailContainer.add(grid, BorderLayout.CENTER);

        // Command line box
        JPanel cmdPanel = new JPanel(new BorderLayout(6, 0));
        cmdPanel.setBackground(UITheme.CARD_BG);
        JLabel cmdLbl = new JLabel("Command Line:");
        cmdLbl.setFont(UITheme.FONT_BOLD);
        cmdLbl.setForeground(UITheme.TEXT_MUTED);
        detailCommandField = UITheme.createTextField(20);
        detailCommandField.setEditable(false);
        detailCommandField.setBackground(UITheme.PANEL_BG);
        cmdPanel.add(cmdLbl, BorderLayout.WEST);
        cmdPanel.add(detailCommandField, BorderLayout.CENTER);
        detailContainer.add(cmdPanel, BorderLayout.SOUTH);

        add(detailContainer, BorderLayout.SOUTH);

        // Search text listener
        DocumentListener searchListener = new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void removeUpdate(DocumentEvent e) { applyFilter(); }
            @Override
            public void changedUpdate(DocumentEvent e) { applyFilter(); }
        };
        searchField.getDocument().addDocumentListener(searchListener);
        searchByNameRadio.addActionListener(e -> applyFilter());
        searchByPidRadio.addActionListener(e -> applyFilter());
    }

    private JPanel createFieldBox(String labelText, JLabel valueLabel) {
        JPanel box = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 2));
        box.setBackground(UITheme.CARD_BG);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MUTED);
        box.add(lbl);
        box.add(valueLabel);
        return box;
    }

    private JLabel createDetailValueLabel(String text) {
        JLabel val = new JLabel(text);
        val.setFont(UITheme.FONT_BODY);
        val.setForeground(UITheme.TEXT_MAIN);
        return val;
    }

    private void showProcessDetails(ProcessInfo info) {
        this.selectedProcess = info;
        detailPidLabel.setText(String.valueOf(info.getPid()));
        detailNameLabel.setText(info.getName());
        detailPpidLabel.setText(String.valueOf(info.getPpid()));
        detailStateLabel.setText(info.getState() != null ? info.getState() : "Unknown");
        detailUserLabel.setText(info.getUser() != null ? info.getUser() : "N/A");
        detailCpuLabel.setText(String.format("%.1f%%", info.getCpuUsage()));
        long kb = info.getMemoryKb();
        double mb = kb / 1024.0;
        detailMemoryLabel.setText(String.format("%,d KB (%.1f MB)", kb, mb));
        detailThreadsLabel.setText(String.valueOf(info.getThreadCount()));

        if (info.getStartTime() > 0) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            detailStartLabel.setText(sdf.format(new Date(info.getStartTime())));
        } else {
            detailStartLabel.setText("N/A");
        }

        detailCommandField.setText(info.getCommand() != null ? info.getCommand() : info.getName());
    }

    private void applyFilter() {
        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            populateTable(allProcesses);
            return;
        }

        List<ProcessInfo> filtered = new ArrayList<>();
        boolean byPid = searchByPidRadio.isSelected();

        for (ProcessInfo p : allProcesses) {
            if (byPid) {
                if (String.valueOf(p.getPid()).contains(query)) {
                    filtered.add(p);
                }
            } else {
                if (p.getName() != null && p.getName().toLowerCase().contains(query.toLowerCase())) {
                    filtered.add(p);
                }
            }
        }
        populateTable(filtered);
    }

    private void populateTable(List<ProcessInfo> list) {
        Long previousSelectedPid = null;
        int sel = processTable.getSelectedRow();
        if (sel >= 0) {
            int modelRow = processTable.convertRowIndexToModel(sel);
            Object obj = tableModel.getValueAt(modelRow, 0);
            if (obj instanceof Long) {
                previousSelectedPid = (Long) obj;
            }
        }

        tableModel.setRowCount(0);
        int restoreRowIndex = -1;

        for (int i = 0; i < list.size(); i++) {
            ProcessInfo p = list.get(i);
            tableModel.addRow(new Object[]{
                    p.getPid(),
                    p.getName(),
                    p.getPpid(),
                    p.getState(),
                    p.getCpuUsage(),
                    p.getMemoryKb(),
                    p.getThreadCount()
            });

            if (previousSelectedPid != null && p.getPid() == previousSelectedPid) {
                restoreRowIndex = i;
            }
        }

        if (restoreRowIndex >= 0 && restoreRowIndex < processTable.getRowCount()) {
            int viewRow = processTable.convertRowIndexToView(restoreRowIndex);
            if (viewRow >= 0 && viewRow < processTable.getRowCount()) {
                processTable.setRowSelectionInterval(viewRow, viewRow);
            }
        }
    }

    /**
     * Updates the process table with new process telemetry data.
     */
    public void updateProcessList(List<ProcessInfo> processes) {
        SwingUtilities.invokeLater(() -> {
            if (processes == null) return;
            this.allProcesses = new ArrayList<>(processes);
            processMap.clear();
            for (ProcessInfo p : processes) {
                processMap.put(p.getPid(), p);
            }

            processCountLabel.setText("Processes: " + processes.size());
            applyFilter();
        });
    }

    public boolean isAutoRefreshEnabled() {
        return autoRefreshCheckBox.isSelected();
    }

    public void setManualRefreshAction(Runnable action) {
        this.manualRefreshAction = action;
    }
}
