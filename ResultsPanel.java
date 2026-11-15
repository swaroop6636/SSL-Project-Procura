package com.procura.ui;

import com.procura.analytics.CsvExporter;
import com.procura.scheduler.Job;
import com.procura.scheduler.SchedulingResult;
import com.procura.scheduler.SchedulingResult.GanttEntry;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * ResultsPanel: Displays CPU scheduling simulation metrics, completed jobs table,
 * performance statistics, and a custom painted 2D Gantt Chart.
 */
public class ResultsPanel extends JPanel {

    private final JLabel algorithmTitleLabel;
    private final JLabel simulationSummaryLabel;
    private final JButton exportCsvButton;

    private final JTable resultsTable;
    private final DefaultTableModel tableModel;

    // Performance Metrics Labels
    private final JLabel avgWaitingLabel;
    private final JLabel avgTurnaroundLabel;
    private final JLabel avgResponseLabel;
    private final JLabel cpuUtilLabel;
    private final JLabel throughputLabel;

    private final GanttChartPanel ganttChartPanel;

    private SchedulingResult currentResult;
    private String currentAlgorithmName = "No Simulation Run";

    private static final String[] COLUMN_NAMES = {
            "Job ID", "Arrival Time", "Burst Time", "Priority",
            "Completion Time", "Turnaround Time", "Waiting Time", "Response Time"
    };

    public ResultsPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. Top Header & Toolbar
        JPanel topContainer = new JPanel(new BorderLayout(10, 8));
        topContainer.setBackground(UITheme.BG_DARK);

        JPanel leftHeader = new JPanel(new BorderLayout(0, 4));
        leftHeader.setBackground(UITheme.BG_DARK);
        algorithmTitleLabel = UITheme.createHeaderLabel("Simulation Results & Analysis");
        simulationSummaryLabel = new JLabel("Run a simulation from Job Scheduler tab to analyze execution metrics.");
        simulationSummaryLabel.setFont(UITheme.FONT_BODY);
        simulationSummaryLabel.setForeground(UITheme.TEXT_MUTED);
        leftHeader.add(algorithmTitleLabel, BorderLayout.NORTH);
        leftHeader.add(simulationSummaryLabel, BorderLayout.SOUTH);
        topContainer.add(leftHeader, BorderLayout.WEST);

        JPanel rightActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        rightActions.setBackground(UITheme.BG_DARK);
        exportCsvButton = UITheme.createPrimaryButton("Export to CSV");
        exportCsvButton.setEnabled(false);
        exportCsvButton.addActionListener(e -> handleExportCsv());
        rightActions.add(exportCsvButton);
        topContainer.add(rightActions, BorderLayout.EAST);

        add(topContainer, BorderLayout.NORTH);

        // 2. Center: Results Table
        tableModel = new DefaultTableModel(COLUMN_NAMES, 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return String.class;
                return Integer.class;
            }
            @Override
            public boolean isCellEditable(int r, int c) { return false; }
        };

        resultsTable = new JTable(tableModel);
        UITheme.styleTable(resultsTable);

        // Custom Cell Renderers
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

        // Color-coded Waiting Time Renderer (col 6)
        DefaultTableCellRenderer waitingTimeRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, col);
                setHorizontalAlignment(CENTER);

                if (!isSelected) {
                    setBackground(row % 2 == 0 ? UITheme.PANEL_BG : UITheme.TABLE_ROW_ALT);
                    if (value instanceof Number) {
                        int wait = ((Number) value).intValue();
                        if (wait <= 4) {
                            setForeground(UITheme.SUCCESS);
                        } else if (wait <= 10) {
                            setForeground(UITheme.WARNING);
                        } else {
                            setForeground(UITheme.ERROR);
                        }
                        setFont(UITheme.FONT_BOLD);
                    }
                } else {
                    setBackground(new Color(50, 80, 130));
                    setForeground(Color.WHITE);
                }
                return this;
            }
        };

        for (int i = 0; i < COLUMN_NAMES.length; i++) {
            if (i == 6) {
                resultsTable.getColumnModel().getColumn(i).setCellRenderer(waitingTimeRenderer);
            } else {
                resultsTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        JScrollPane tableScroll = new JScrollPane(resultsTable);
        UITheme.styleScrollPane(tableScroll);
        tableScroll.setPreferredSize(new Dimension(0, 200));

        // 3. Bottom Section: Left = Stats, Right = Gantt Chart
        JPanel bottomContainer = new JPanel(new GridLayout(1, 2, 14, 0));
        bottomContainer.setBackground(UITheme.BG_DARK);
        bottomContainer.setPreferredSize(new Dimension(0, 280));

        // Bottom Left: Performance Statistics Panel
        JPanel statsPanel = new JPanel(new BorderLayout(0, 10));
        statsPanel.setBackground(UITheme.CARD_BG);
        statsPanel.setBorder(UITheme.createCardBorder());

        JLabel statsHeader = new JLabel("Performance Statistics");
        statsHeader.setFont(UITheme.FONT_SUBHEADER);
        statsHeader.setForeground(UITheme.TEXT_MAIN);
        statsPanel.add(statsHeader, BorderLayout.NORTH);

        JPanel metricsGrid = new JPanel(new GridLayout(5, 1, 6, 8));
        metricsGrid.setBackground(UITheme.CARD_BG);

        avgWaitingLabel = createStatDisplay();
        avgTurnaroundLabel = createStatDisplay();
        avgResponseLabel = createStatDisplay();
        cpuUtilLabel = createStatDisplay();
        throughputLabel = createStatDisplay();

        metricsGrid.add(createMetricCard("Average Waiting Time (AWT):", avgWaitingLabel, UITheme.ACCENT_PRIMARY));
        metricsGrid.add(createMetricCard("Average Turnaround Time (ATT):", avgTurnaroundLabel, UITheme.ACCENT_PRIMARY));
        metricsGrid.add(createMetricCard("Average Response Time (ART):", avgResponseLabel, UITheme.ACCENT_PRIMARY));
        metricsGrid.add(createMetricCard("CPU Utilization:", cpuUtilLabel, UITheme.SUCCESS));
        metricsGrid.add(createMetricCard("Throughput (Completed / Time):", throughputLabel, UITheme.WARNING));

        statsPanel.add(metricsGrid, BorderLayout.CENTER);
        bottomContainer.add(statsPanel);

        // Bottom Right: Gantt Chart Panel
        JPanel ganttContainer = new JPanel(new BorderLayout(0, 8));
        ganttContainer.setBackground(UITheme.CARD_BG);
        ganttContainer.setBorder(UITheme.createCardBorder());

        JLabel ganttHeader = new JLabel("Execution Timeline (Gantt Chart)");
        ganttHeader.setFont(UITheme.FONT_SUBHEADER);
        ganttHeader.setForeground(UITheme.TEXT_MAIN);
        ganttContainer.add(ganttHeader, BorderLayout.NORTH);

        ganttChartPanel = new GanttChartPanel();
        JScrollPane ganttScroll = new JScrollPane(ganttChartPanel);
        UITheme.styleScrollPane(ganttScroll);
        ganttScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        ganttScroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER);
        ganttContainer.add(ganttScroll, BorderLayout.CENTER);

        bottomContainer.add(ganttContainer);

        // Combine Center and Bottom in a split pane
        JSplitPane centerSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, tableScroll, bottomContainer);
        centerSplit.setResizeWeight(0.45);
        centerSplit.setBackground(UITheme.BG_DARK);
        centerSplit.setDividerSize(6);
        centerSplit.setBorder(null);

        add(centerSplit, BorderLayout.CENTER);
    }

    private JPanel createMetricCard(String title, JLabel valLabel, Color accent) {
        JPanel card = new JPanel(new BorderLayout(8, 0));
        card.setBackground(UITheme.PANEL_BG);
        card.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1), new EmptyBorder(6, 12, 6, 12)));

        JLabel lbl = new JLabel(title);
        lbl.setFont(UITheme.FONT_BOLD);
        lbl.setForeground(UITheme.TEXT_MAIN);

        valLabel.setForeground(accent);

        card.add(lbl, BorderLayout.WEST);
        card.add(valLabel, BorderLayout.EAST);
        return card;
    }

    private JLabel createStatDisplay() {
        JLabel lbl = new JLabel("-", SwingConstants.RIGHT);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lbl.setForeground(UITheme.TEXT_MAIN);
        return lbl;
    }

    private void handleExportCsv() {
        if (currentResult == null) {
            JOptionPane.showMessageDialog(this, "No simulation results available to export.", "Export Warning", JOptionPane.WARNING_MESSAGE);
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Scheduling Results to CSV");
        fileChooser.setSelectedFile(new File("scheduling_results.csv"));
        fileChooser.setFileFilter(new FileNameExtensionFilter("CSV Files (*.csv)", "csv"));

        int userSelection = fileChooser.showSaveDialog(this);
        if (userSelection == JFileChooser.APPROVE_OPTION) {
            File fileToSave = fileChooser.getSelectedFile();
            String path = fileToSave.getAbsolutePath();
            if (!path.toLowerCase().endsWith(".csv")) {
                path += ".csv";
            }
            try {
                CsvExporter.exportSchedulingResults(currentResult, path);
                JOptionPane.showMessageDialog(this, "Results successfully exported to:\n" + path, "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Failed to export CSV: " + ex.getMessage(), "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Updates and displays the scheduling simulation results.
     */
    public void displayResults(SchedulingResult result, String algorithmName) {
        SwingUtilities.invokeLater(() -> {
            if (result == null) return;
            this.currentResult = result;
            this.currentAlgorithmName = algorithmName != null ? algorithmName : result.getAlgorithmName();

            algorithmTitleLabel.setText("Simulation Results: " + this.currentAlgorithmName);
            simulationSummaryLabel.setText(String.format("Completed %d jobs | Total Simulation Time: %d units",
                    result.getCompletedJobs() != null ? result.getCompletedJobs().size() : 0,
                    result.getTotalSimulationTime()));

            exportCsvButton.setEnabled(true);

            // Populate Table
            tableModel.setRowCount(0);
            if (result.getCompletedJobs() != null) {
                for (Job j : result.getCompletedJobs()) {
                    tableModel.addRow(new Object[]{
                            j.getJobId(),
                            j.getArrivalTime(),
                            j.getBurstTime(),
                            j.getPriority(),
                            j.getCompletionTime(),
                            j.getTurnaroundTime(),
                            j.getWaitingTime(),
                            j.getResponseTime()
                    });
                }
            }

            // Populate Performance Metrics
            avgWaitingLabel.setText(String.format("%.2f units", result.getAvgWaitingTime()));
            avgTurnaroundLabel.setText(String.format("%.2f units", result.getAvgTurnaroundTime()));
            avgResponseLabel.setText(String.format("%.2f units", result.getAvgResponseTime()));
            cpuUtilLabel.setText(String.format("%.1f%%", result.getCpuUtilization()));
            throughputLabel.setText(String.format("%.4f jobs/unit", result.getThroughput()));

            // Update Gantt Chart
            ganttChartPanel.setGanttData(result.getGanttChart(), result.getTotalSimulationTime());
        });
    }

    /**
     * Custom JPanel for drawing 2D Gantt Chart execution timeline.
     */
    public static class GanttChartPanel extends JPanel {
        private List<GanttEntry> ganttEntries = new ArrayList<>();
        private int totalSimulationTime = 0;

        private static final Color[] JOB_PALETTE = {
                new Color(41, 128, 185), // Blue
                new Color(39, 174, 96),  // Green
                new Color(230, 126, 34), // Orange
                new Color(142, 68, 173), // Purple
                new Color(22, 160, 133), // Teal
                new Color(231, 76, 60),  // Red
                new Color(243, 156, 18), // Amber
                new Color(52, 152, 219), // Light Blue
                new Color(155, 89, 182), // Lilac
                new Color(46, 204, 113)  // Emerald
        };
        private static final Color IDLE_COLOR = new Color(75, 75, 75);

        private final Map<String, Color> jobColorMap = new HashMap<>();

        public GanttChartPanel() {
            setBackground(UITheme.PANEL_BG);
            setPreferredSize(new Dimension(650, 180));
        }

        public void setGanttData(List<GanttEntry> entries, int totalTime) {
            this.ganttEntries = entries != null ? new ArrayList<>(entries) : new ArrayList<>();
            this.totalSimulationTime = Math.max(totalTime, calculateTotalTimeFromEntries(this.ganttEntries));

            // Assign colors
            jobColorMap.clear();
            int colorIndex = 0;
            for (GanttEntry e : this.ganttEntries) {
                if (e.jobId != null && !e.jobId.equalsIgnoreCase("IDLE") && !jobColorMap.containsKey(e.jobId)) {
                    jobColorMap.put(e.jobId, JOB_PALETTE[colorIndex % JOB_PALETTE.length]);
                    colorIndex++;
                }
            }

            // Adjust width if timeline is long
            int requiredWidth = Math.max(650, this.totalSimulationTime * 35 + 80);
            setPreferredSize(new Dimension(requiredWidth, 180));
            revalidate();
            repaint();
        }

        private int calculateTotalTimeFromEntries(List<GanttEntry> entries) {
            int max = 0;
            for (GanttEntry e : entries) {
                if (e.endTime > max) max = e.endTime;
            }
            return max;
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            if (ganttEntries.isEmpty() || totalSimulationTime <= 0) {
                g2.setColor(UITheme.TEXT_MUTED);
                g2.setFont(UITheme.FONT_BODY);
                String msg = "Gantt timeline will be generated after running a simulation.";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g2.drawString(msg, Math.max(20, x), y);
                g2.dispose();
                return;
            }

            int startX = 30;
            int chartY = 35;
            int barHeight = 45;
            int availableWidth = getWidth() - startX - 40;
            double scale = (double) availableWidth / Math.max(1, totalSimulationTime);

            // Draw Bars
            for (GanttEntry entry : ganttEntries) {
                int x1 = startX + (int) Math.round(entry.startTime * scale);
                int x2 = startX + (int) Math.round(entry.endTime * scale);
                int barWidth = Math.max(2, x2 - x1);

                boolean isIdle = entry.jobId == null || entry.jobId.equalsIgnoreCase("IDLE");
                Color barColor = isIdle ? IDLE_COLOR : jobColorMap.getOrDefault(entry.jobId, UITheme.ACCENT_PRIMARY);

                // Bar fill
                g2.setColor(barColor);
                g2.fillRoundRect(x1, chartY, barWidth, barHeight, 6, 6);

                // Bar border
                g2.setColor(Color.WHITE.darker());
                g2.drawRoundRect(x1, chartY, barWidth, barHeight, 6, 6);

                // Label inside bar
                String label = isIdle ? "Idle" : entry.jobId;
                g2.setFont(UITheme.FONT_BOLD);
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(label);
                if (barWidth >= textW + 4) {
                    g2.setColor(Color.WHITE);
                    int textX = x1 + (barWidth - textW) / 2;
                    int textY = chartY + (barHeight + fm.getAscent() - fm.getDescent()) / 2;
                    g2.drawString(label, textX, textY);
                }
            }

            // Draw Time Axis at bottom of bars
            int axisY = chartY + barHeight + 10;
            g2.setColor(UITheme.BORDER);
            g2.drawLine(startX, axisY, startX + availableWidth, axisY);

            // Time Tick Marks & Labels
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            FontMetrics axisFm = g2.getFontMetrics();

            Set<Integer> timePoints = new TreeSet<>();
            timePoints.add(0);
            timePoints.add(totalSimulationTime);
            for (GanttEntry e : ganttEntries) {
                timePoints.add(e.startTime);
                timePoints.add(e.endTime);
            }

            for (int t : timePoints) {
                int tickX = startX + (int) Math.round(t * scale);
                g2.setColor(UITheme.TEXT_MUTED);
                g2.drawLine(tickX, axisY - 4, tickX, axisY + 4);

                String tStr = String.valueOf(t);
                int tw = axisFm.stringWidth(tStr);
                g2.drawString(tStr, tickX - (tw / 2), axisY + 18);
            }

            // Legend at top
            int legendX = startX;
            int legendY = 18;
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));

            // Idle legend
            g2.setColor(IDLE_COLOR);
            g2.fillRect(legendX, legendY - 9, 10, 10);
            g2.setColor(UITheme.TEXT_MUTED);
            g2.drawString("Idle", legendX + 14, legendY);
            legendX += 50;

            for (Map.Entry<String, Color> entry : jobColorMap.entrySet()) {
                g2.setColor(entry.getValue());
                g2.fillRect(legendX, legendY - 9, 10, 10);
                g2.setColor(UITheme.TEXT_MAIN);
                g2.drawString(entry.getKey(), legendX + 14, legendY);
                legendX += axisFm.stringWidth(entry.getKey()) + 26;
                if (legendX > getWidth() - 50) break;
            }

            g2.dispose();
        }
    }
}
