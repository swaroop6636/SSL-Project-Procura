package com.procura.ui;

import com.procura.scheduler.*;
import com.procura.util.ValidationUtil;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * SchedulerPanel: Job submission form, algorithm selector, and job queue management.
 */
public class SchedulerPanel extends JPanel {

    private final JTextField jobIdField;
    private final JSpinner arrivalSpinner;
    private final JSpinner burstSpinner;
    private final JSpinner prioritySpinner;
    private final JComboBox<String> algorithmCombo;
    private final JSpinner quantumSpinner;
    private final JLabel quantumLabel;
    private final JLabel statusMessageLabel;

    private final JTable queueTable;
    private final DefaultTableModel tableModel;
    private final JLabel queueStatsLabel;

    private final List<Job> jobList = new ArrayList<>();
    private int nextJobNumber = 1;

    private ResultsPanel resultsPanel;
    private JTabbedPane parentTabbedPane;
    private DashboardPanel dashboardPanel;

    private static final String[] ALGORITHMS = {
            "FIFO (First Come First Served)",
            "Priority (Non-Preemptive)",
            "Round Robin"
    };

    private static final String[] COLUMN_NAMES = {
            "Job ID", "Arrival Time", "Burst Time", "Priority", "Status"
    };

    public SchedulerPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Title Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_DARK);
        JLabel titleLabel = UITheme.createHeaderLabel("CPU Job Scheduling Simulation");
        JLabel subtitle = new JLabel("Configure process burst requirements and evaluate scheduling algorithms");
        subtitle.setFont(UITheme.FONT_BODY);
        subtitle.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subtitle, BorderLayout.SOUTH);
        add(headerPanel, BorderLayout.NORTH);

        // Main content: Left Form, Right Table
        JPanel mainContent = new JPanel(new GridLayout(1, 2, 16, 0));
        mainContent.setBackground(UITheme.BG_DARK);

        // ==================== LEFT: JOB INPUT FORM ====================
        JPanel formContainer = new JPanel(new BorderLayout(0, 10));
        formContainer.setBackground(UITheme.CARD_BG);
        formContainer.setBorder(UITheme.createCardBorder());

        JLabel formHeader = new JLabel("Job Configuration & Algorithm Parameters");
        formHeader.setFont(UITheme.FONT_SUBHEADER);
        formHeader.setForeground(UITheme.TEXT_MAIN);
        formHeader.setBorder(new EmptyBorder(0, 0, 8, 0));
        formContainer.add(formHeader, BorderLayout.NORTH);

        JPanel formGrid = new JPanel(new GridBagLayout());
        formGrid.setBackground(UITheme.CARD_BG);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 4, 6, 4);

        // 1. Job ID
        gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.3;
        formGrid.add(createFormLabel("Job Identifier:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 0.7;
        jobIdField = UITheme.createTextField(10);
        jobIdField.setText("J" + nextJobNumber);
        formGrid.add(jobIdField, gbc);

        // 2. Arrival Time
        gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.3;
        formGrid.add(createFormLabel("Arrival Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 0.7;
        arrivalSpinner = UITheme.createSpinner(new SpinnerNumberModel(0, 0, 999, 1));
        formGrid.add(arrivalSpinner, gbc);

        // 3. Burst Time
        gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.3;
        formGrid.add(createFormLabel("CPU Burst Time:"), gbc);
        gbc.gridx = 1; gbc.gridy = 2; gbc.weightx = 0.7;
        burstSpinner = UITheme.createSpinner(new SpinnerNumberModel(5, 1, 999, 1));
        formGrid.add(burstSpinner, gbc);

        // 4. Priority
        gbc.gridx = 0; gbc.gridy = 3; gbc.weightx = 0.3;
        formGrid.add(createFormLabel("Priority (Lower = Higher):"), gbc);
        gbc.gridx = 1; gbc.gridy = 3; gbc.weightx = 0.7;
        prioritySpinner = UITheme.createSpinner(new SpinnerNumberModel(1, 0, 99, 1));
        formGrid.add(prioritySpinner, gbc);

        // Separator
        gbc.gridx = 0; gbc.gridy = 4; gbc.gridwidth = 2;
        JSeparator sep = new JSeparator();
        sep.setForeground(UITheme.BORDER);
        formGrid.add(sep, gbc);
        gbc.gridwidth = 1;

        // 5. Algorithm Selector
        gbc.gridx = 0; gbc.gridy = 5; gbc.weightx = 0.3;
        formGrid.add(createFormLabel("Scheduling Algorithm:"), gbc);
        gbc.gridx = 1; gbc.gridy = 5; gbc.weightx = 0.7;
        algorithmCombo = UITheme.createComboBox(ALGORITHMS);
        formGrid.add(algorithmCombo, gbc);

        // 6. Time Quantum
        gbc.gridx = 0; gbc.gridy = 6; gbc.weightx = 0.3;
        quantumLabel = createFormLabel("Time Quantum (RR):");
        quantumLabel.setEnabled(false);
        formGrid.add(quantumLabel, gbc);
        gbc.gridx = 1; gbc.gridy = 6; gbc.weightx = 0.7;
        quantumSpinner = UITheme.createSpinner(new SpinnerNumberModel(2, 1, 100, 1));
        quantumSpinner.setEnabled(false);
        formGrid.add(quantumSpinner, gbc);

        // Algorithm change listener to toggle quantum
        algorithmCombo.addActionListener(e -> {
            boolean isRR = algorithmCombo.getSelectedItem() != null &&
                    algorithmCombo.getSelectedItem().toString().contains("Round Robin");
            quantumLabel.setEnabled(isRR);
            quantumSpinner.setEnabled(isRR);
        });

        // 7. Status feedback label
        gbc.gridx = 0; gbc.gridy = 7; gbc.gridwidth = 2;
        statusMessageLabel = new JLabel(" ");
        statusMessageLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusMessageLabel.setForeground(UITheme.SUCCESS);
        formGrid.add(statusMessageLabel, gbc);
        gbc.gridwidth = 1;

        formContainer.add(formGrid, BorderLayout.CENTER);

        // Buttons at bottom of form
        JPanel buttonBox = new JPanel(new BorderLayout(0, 8));
        buttonBox.setBackground(UITheme.CARD_BG);

        JPanel actionRow = new JPanel(new GridLayout(2, 2, 8, 8));
        actionRow.setBackground(UITheme.CARD_BG);

        JButton addJobBtn = UITheme.createPrimaryButton("+ Add Job");
        addJobBtn.addActionListener(e -> handleAddJob());

        JButton removeJobBtn = UITheme.createSecondaryButton("Remove Selected");
        removeJobBtn.addActionListener(e -> handleRemoveSelected());

        JButton clearAllBtn = UITheme.createSecondaryButton("Clear All Jobs");
        clearAllBtn.addActionListener(e -> handleClearAll());

        JButton sampleBtn = UITheme.createSecondaryButton("Load Sample Jobs");
        sampleBtn.addActionListener(e -> handleLoadSampleJobs());

        actionRow.add(addJobBtn);
        actionRow.add(removeJobBtn);
        actionRow.add(sampleBtn);
        actionRow.add(clearAllBtn);
        buttonBox.add(actionRow, BorderLayout.NORTH);

        JButton runSchedulerBtn = UITheme.createPrimaryButton("▶  RUN SCHEDULER & VIEW RESULTS");
        runSchedulerBtn.setFont(new Font("Segoe UI", Font.BOLD, 14));
        runSchedulerBtn.setPreferredSize(new Dimension(0, 44));
        runSchedulerBtn.setBackground(new Color(40, 160, 90)); // Highlighted run button
        runSchedulerBtn.addActionListener(e -> handleRunScheduler());
        buttonBox.add(runSchedulerBtn, BorderLayout.SOUTH);

        formContainer.add(buttonBox, BorderLayout.SOUTH);
        mainContent.add(formContainer);

        // ==================== RIGHT: JOB QUEUE TABLE ====================
        JPanel tableContainer = new JPanel(new BorderLayout(0, 8));
        tableContainer.setBackground(UITheme.CARD_BG);
        tableContainer.setBorder(UITheme.createCardBorder());

        JPanel tableHeader = new JPanel(new BorderLayout());
        tableHeader.setBackground(UITheme.CARD_BG);
        JLabel queueTitle = new JLabel("Pending Job Queue");
        queueTitle.setFont(UITheme.FONT_SUBHEADER);
        queueTitle.setForeground(UITheme.TEXT_MAIN);

        queueStatsLabel = new JLabel("Total Jobs: 0 | Total Burst: 0");
        queueStatsLabel.setFont(UITheme.FONT_BODY);
        queueStatsLabel.setForeground(UITheme.TEXT_MUTED);

        tableHeader.add(queueTitle, BorderLayout.WEST);
        tableHeader.add(queueStatsLabel, BorderLayout.EAST);
        tableContainer.add(tableHeader, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                switch (columnIndex) {
                    case 1: // Arrival
                    case 2: // Burst
                    case 3: // Priority
                        return Integer.class;
                    default:
                        return String.class;
                }
            }
            @Override
            public boolean isCellEditable(int row, int col) { return false; }
        };

        queueTable = new JTable(tableModel);
        UITheme.styleTable(queueTable);
        queueTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);
                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UITheme.PANEL_BG : UITheme.TABLE_ROW_ALT);
                    setForeground(UITheme.TEXT_MAIN);
                } else {
                    setBackground(new Color(50, 80, 130));
                    setForeground(Color.WHITE);
                }
                return this;
            }
        };

        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            queueTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
        }

        JScrollPane tableScroll = new JScrollPane(queueTable);
        UITheme.styleScrollPane(tableScroll);
        tableContainer.add(tableScroll, BorderLayout.CENTER);

        mainContent.add(tableContainer);
        add(mainContent, BorderLayout.CENTER);
    }

    private JLabel createFormLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_MAIN);
        return l;
    }

    private void handleAddJob() {
        String jobId = jobIdField.getText().trim();
        if (jobId.isEmpty()) {
            showError("Job ID cannot be blank.");
            return;
        }

        for (Job j : jobList) {
            if (j.getJobId().equalsIgnoreCase(jobId)) {
                showError("A job with ID '" + jobId + "' already exists in the queue.");
                return;
            }
        }

        int arrival = (Integer) arrivalSpinner.getValue();
        int burst = (Integer) burstSpinner.getValue();
        int priority = (Integer) prioritySpinner.getValue();

        if (!ValidationUtil.validateArrivalTime(arrival)) {
            showError("Arrival time must be non-negative (>= 0).");
            return;
        }
        if (!ValidationUtil.validateBurstTime(burst)) {
            showError("Burst time must be strictly positive (> 0).");
            return;
        }
        if (!ValidationUtil.validatePriority(priority)) {
            showError("Priority must be non-negative (>= 0).");
            return;
        }

        Job job = new Job(jobId, arrival, burst, priority);
        jobList.add(job);
        tableModel.addRow(new Object[]{jobId, arrival, burst, priority, "READY"});

        nextJobNumber++;
        jobIdField.setText("J" + nextJobNumber);
        updateQueueStats();
        showSuccess("Job " + jobId + " added to queue.");

        if (dashboardPanel != null) {
            dashboardPanel.addActivity("Scheduler: Added job " + jobId + " (burst=" + burst + ", priority=" + priority + ")");
        }
    }

    private void handleRemoveSelected() {
        int selectedRow = queueTable.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select a job from the table to remove.");
            return;
        }
        String removedId = (String) tableModel.getValueAt(selectedRow, 0);
        jobList.remove(selectedRow);
        tableModel.removeRow(selectedRow);
        updateQueueStats();
        showSuccess("Removed job " + removedId + " from queue.");
    }

    private void handleClearAll() {
        if (jobList.isEmpty()) return;
        jobList.clear();
        tableModel.setRowCount(0);
        nextJobNumber = 1;
        jobIdField.setText("J" + nextJobNumber);
        updateQueueStats();
        showSuccess("Cleared all jobs from queue.");
    }

    private void handleLoadSampleJobs() {
        handleClearAll();
        Job[] samples = {
                new Job("J1", 0, 8, 3),
                new Job("J2", 1, 4, 1),
                new Job("J3", 2, 9, 4),
                new Job("J4", 3, 5, 2),
                new Job("J5", 4, 2, 5)
        };
        for (Job j : samples) {
            jobList.add(j);
            tableModel.addRow(new Object[]{j.getJobId(), j.getArrivalTime(), j.getBurstTime(), j.getPriority(), "READY"});
        }
        nextJobNumber = 6;
        jobIdField.setText("J" + nextJobNumber);
        updateQueueStats();
        showSuccess("Loaded 5 standard textbook benchmark jobs.");
    }

    private void handleRunScheduler() {
        if (jobList.isEmpty()) {
            showError("Cannot run simulation: Job queue is empty! Add jobs first.");
            JOptionPane.showMessageDialog(this, "The job queue is empty. Please add at least one job or click 'Load Sample Jobs'.",
                    "Empty Queue", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String algoChoice = (String) algorithmCombo.getSelectedItem();
        Scheduler scheduler;

        if (algoChoice != null && algoChoice.contains("FIFO")) {
            scheduler = new FifoScheduler();
        } else if (algoChoice != null && algoChoice.contains("Priority")) {
            scheduler = new PriorityScheduler();
        } else {
            int quantum = (Integer) quantumSpinner.getValue();
            if (!ValidationUtil.validateTimeQuantum(quantum)) {
                showError("Time quantum must be strictly positive (> 0).");
                return;
            }
            scheduler = new RoundRobinScheduler(quantum);
        }

        try {
            // Clone jobs so original list is untouched
            List<Job> jobsToSchedule = new ArrayList<>();
            for (Job j : jobList) {
                jobsToSchedule.add(j.copy());
            }

            SchedulingResult result = scheduler.schedule(jobsToSchedule);

            showSuccess("Scheduled " + jobsToSchedule.size() + " jobs successfully using " + algoChoice + "!");

            if (dashboardPanel != null) {
                dashboardPanel.addActivity("Scheduler: Executed " + algoChoice + " on " + jobsToSchedule.size() +
                        " jobs. Avg Turnaround: " + String.format("%.2f", result.getAvgTurnaroundTime()));
            }

            if (resultsPanel != null) {
                resultsPanel.displayResults(result, algoChoice);
            }

            if (parentTabbedPane != null) {
                parentTabbedPane.setSelectedIndex(4); // Switch to Results tab
            }
        } catch (Exception ex) {
            showError("Simulation error: " + ex.getMessage());
            JOptionPane.showMessageDialog(this, "Scheduler failed: " + ex.getMessage(), "Execution Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateQueueStats() {
        int totalBurst = 0;
        for (Job j : jobList) {
            totalBurst += j.getBurstTime();
        }
        queueStatsLabel.setText("Total Jobs: " + jobList.size() + " | Total Burst: " + totalBurst);
    }

    private void showError(String msg) {
        statusMessageLabel.setForeground(UITheme.ERROR);
        statusMessageLabel.setText("✖ " + msg);
    }

    private void showSuccess(String msg) {
        statusMessageLabel.setForeground(UITheme.SUCCESS);
        statusMessageLabel.setText("✔ " + msg);
    }

    public int getJobCount() {
        return jobList.size();
    }

    public List<Job> getJobList() {
        return Collections.unmodifiableList(jobList);
    }

    public void setResultsTarget(ResultsPanel resultsPanel, JTabbedPane tabbedPane, DashboardPanel dashboardPanel) {
        this.resultsPanel = resultsPanel;
        this.parentTabbedPane = tabbedPane;
        this.dashboardPanel = dashboardPanel;
    }
}
