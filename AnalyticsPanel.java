package com.procura.ui;

import com.procura.analytics.PerformanceAnalyzer;
import com.procura.util.SystemInfo;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * AnalyticsPanel: Performance analytics tab for generating and viewing Python/Matplotlib
 * telemetry graphs and exporting historical monitoring data.
 */
public class AnalyticsPanel extends JPanel {

    private final JComboBox<GraphOption> graphSelectorCombo;
    private final JLabel imageDisplayLabel;
    private final JScrollPane imageScrollPane;
    private final JLabel statusLabel;
    private final JProgressBar progressBar;

    private PerformanceAnalyzer performanceAnalyzer;

    private static final String DATA_DIR = "data";
    private static final String GRAPHS_DIR = "graphs";
    private static final String PYTHON_DIR = "scripts";

    public static class GraphOption {
        final String displayName;
        final String fileName;

        public GraphOption(String displayName, String fileName) {
            this.displayName = displayName;
            this.fileName = fileName;
        }

        @Override
        public String toString() {
            return displayName;
        }
    }

    private static final GraphOption[] GRAPH_OPTIONS = {
            new GraphOption("CPU Utilization History", "cpu_usage.png"),
            new GraphOption("Memory Utilization History", "memory_usage.png"),
            new GraphOption("Algorithm Comparison (TAT & WT)", "algorithm_comparison.png"),
            new GraphOption("Simulation Gantt Timeline", "gantt_chart.png")
    };

    public AnalyticsPanel() {
        setLayout(new BorderLayout(14, 14));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // 1. Header & Controls
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setBackground(UITheme.BG_DARK);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_DARK);
        JLabel titleLabel = UITheme.createHeaderLabel("Performance Analytics & Visualizations");
        JLabel subLabel = new JLabel("Generate Matplotlib telemetry charts and compare scheduling algorithms");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        // Action Toolbar
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
        toolbar.setBackground(UITheme.PANEL_BG);
        toolbar.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1), new EmptyBorder(8, 10, 8, 10)));

        JButton btnCpuPlot = UITheme.createSecondaryButton("Generate CPU Plot");
        btnCpuPlot.addActionListener(e -> generatePlotsAsync("cpu"));

        JButton btnMemPlot = UITheme.createSecondaryButton("Generate Memory Plot");
        btnMemPlot.addActionListener(e -> generatePlotsAsync("memory"));

        JButton btnComparePlot = UITheme.createSecondaryButton("Generate Scheduling Comparison");
        btnComparePlot.addActionListener(e -> generatePlotsAsync("comparison"));

        JButton btnAllPlots = UITheme.createPrimaryButton("Generate All Plots");
        btnAllPlots.addActionListener(e -> generatePlotsAsync("all"));

        JButton btnExportData = UITheme.createSecondaryButton("Export Raw Data");
        btnExportData.addActionListener(e -> handleExportData());

        toolbar.add(btnAllPlots);
        toolbar.add(btnCpuPlot);
        toolbar.add(btnMemPlot);
        toolbar.add(btnComparePlot);
        toolbar.add(btnExportData);

        topContainer.add(toolbar, BorderLayout.CENTER);

        // Selector Bar & Status Bar
        JPanel subBar = new JPanel(new BorderLayout(10, 4));
        subBar.setBackground(UITheme.BG_DARK);

        JPanel selectBox = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 2));
        selectBox.setBackground(UITheme.BG_DARK);
        JLabel selectLbl = new JLabel("Display Graph:");
        selectLbl.setFont(UITheme.FONT_BOLD);
        selectLbl.setForeground(UITheme.TEXT_MAIN);
        selectBox.add(selectLbl);

        graphSelectorCombo = UITheme.createComboBox(GRAPH_OPTIONS);
        graphSelectorCombo.setPreferredSize(new Dimension(280, 28));
        graphSelectorCombo.addActionListener(e -> loadSelectedGraph());
        selectBox.add(graphSelectorCombo);

        JButton reloadBtn = UITheme.createSecondaryButton("Reload Image");
        reloadBtn.addActionListener(e -> loadSelectedGraph());
        selectBox.add(reloadBtn);
        subBar.add(selectBox, BorderLayout.WEST);

        // Progress bar and status
        JPanel statusBox = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 2));
        statusBox.setBackground(UITheme.BG_DARK);

        progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setVisible(false);
        progressBar.setPreferredSize(new Dimension(100, 18));
        statusBox.add(progressBar);

        statusLabel = new JLabel("Ready. Click a generation button above.");
        statusLabel.setFont(UITheme.FONT_BODY);
        statusLabel.setForeground(UITheme.TEXT_MUTED);
        statusBox.add(statusLabel);

        subBar.add(statusBox, BorderLayout.EAST);
        topContainer.add(subBar, BorderLayout.SOUTH);

        add(topContainer, BorderLayout.NORTH);

        // 2. Center Image Display
        imageDisplayLabel = new JLabel("", SwingConstants.CENTER);
        imageDisplayLabel.setForeground(UITheme.TEXT_MUTED);
        imageDisplayLabel.setFont(UITheme.FONT_BODY);

        imageScrollPane = new JScrollPane(imageDisplayLabel);
        UITheme.styleScrollPane(imageScrollPane);
        imageScrollPane.setBackground(UITheme.CARD_BG);
        imageScrollPane.getViewport().setBackground(UITheme.CARD_BG);

        add(imageScrollPane, BorderLayout.CENTER);

        // Initial check for python
        checkEnvironment();
        loadSelectedGraph();
    }

    public void setPerformanceAnalyzer(PerformanceAnalyzer analyzer) {
        this.performanceAnalyzer = analyzer;
    }

    private void checkEnvironment() {
        boolean pythonOk = SystemInfo.isPythonAvailable();
        if (!pythonOk) {
            statusLabel.setForeground(UITheme.WARNING);
            statusLabel.setText("Note: Python not found in PATH. You can still export telemetry CSVs.");
        }
    }

    private void handleExportData() {
        if (performanceAnalyzer == null) {
            showStatus("Analyzer is not connected.", UITheme.ERROR);
            return;
        }

        try {
            File dir = new File(DATA_DIR);
            if (!dir.exists()) dir.mkdirs();

            performanceAnalyzer.exportAllData(DATA_DIR);
            showStatus("Data successfully exported to '" + dir.getAbsolutePath() + "'.", UITheme.SUCCESS);
            JOptionPane.showMessageDialog(this,
                    "Historical monitoring telemetry and simulation metrics exported to folder:\n" + dir.getAbsolutePath(),
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            showStatus("Export failed: " + ex.getMessage(), UITheme.ERROR);
            JOptionPane.showMessageDialog(this, "Failed to export data: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void generatePlotsAsync(String targetPlot) {
        if (!SystemInfo.isPythonAvailable()) {
            JOptionPane.showMessageDialog(this,
                    "Python runtime was not detected on system PATH.\nPlease install Python 3 with 'matplotlib' and 'numpy' to generate graphs.",
                    "Python Not Available", JOptionPane.WARNING_MESSAGE);
            showStatus("Python not available. Plot generation cancelled.", UITheme.WARNING);
            return;
        }

        if (performanceAnalyzer == null) {
            showStatus("Analyzer not connected.", UITheme.ERROR);
            return;
        }

        progressBar.setVisible(true);
        showStatus("Exporting data and running Python visualization script...", UITheme.ACCENT_PRIMARY);

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            private String errorMessage = null;

            @Override
            protected Void doInBackground() {
                try {
                    File dataFolder = new File(DATA_DIR);
                    if (!dataFolder.exists()) dataFolder.mkdirs();

                    File graphFolder = new File(GRAPHS_DIR);
                    if (!graphFolder.exists()) graphFolder.mkdirs();

                    File pythonFolder = new File(PYTHON_DIR);
                    if (!pythonFolder.exists()) pythonFolder.mkdirs();

                    // Export data first
                    performanceAnalyzer.exportAllData(DATA_DIR);

                    // Run python plots
                    performanceAnalyzer.runPythonPlots(PYTHON_DIR, DATA_DIR, GRAPHS_DIR);
                } catch (Exception ex) {
                    errorMessage = ex.getMessage();
                }
                return null;
            }

            @Override
            protected void done() {
                progressBar.setVisible(false);
                if (errorMessage != null) {
                    showStatus("Plot generation failed: " + errorMessage, UITheme.ERROR);
                    JOptionPane.showMessageDialog(AnalyticsPanel.this,
                            "Failed to generate plots:\n" + errorMessage, "Plot Generation Error", JOptionPane.ERROR_MESSAGE);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
                    showStatus("Plots successfully generated at " + sdf.format(new Date()), UITheme.SUCCESS);
                    loadSelectedGraph();
                }
            }
        };

        worker.execute();
    }

    private void loadSelectedGraph() {
        GraphOption opt = (GraphOption) graphSelectorCombo.getSelectedItem();
        if (opt == null) return;

        File graphFile = new File(GRAPHS_DIR, opt.fileName);
        if (!graphFile.exists()) {
            // Also check current directory
            File alt = new File(opt.fileName);
            if (alt.exists()) {
                graphFile = alt;
            }
        }

        if (graphFile.exists()) {
            try {
                BufferedImage img = ImageIO.read(graphFile);
                if (img != null) {
                    imageDisplayLabel.setIcon(new ImageIcon(img));
                    imageDisplayLabel.setText("");
                    return;
                }
            } catch (Exception ex) {
                // Ignore read error and fallback to placeholder
            }
        }

        // Placeholder when image is not yet generated
        imageDisplayLabel.setIcon(null);
        imageDisplayLabel.setText("<html><div style='text-align: center; color: #a0a0a0; padding: 40px;'>"
                + "<h2>" + opt.displayName + "</h2>"
                + "<p>Plot file not found at: <code>" + graphFile.getPath() + "</code></p>"
                + "<p style='margin-top: 15px;'>Click <b>'Generate All Plots'</b> or the specific plot button above to produce this graph.</p>"
                + "</div></html>");
    }

    private void showStatus(String text, Color color) {
        statusLabel.setForeground(color);
        statusLabel.setText(text);
    }
}
