package com.procura.ui;

import com.procura.analytics.PerformanceAnalyzer;
import com.procura.monitoring.CpuMonitor;
import com.procura.monitoring.MemoryMonitor;
import com.procura.monitoring.ProcessHierarchy;
import com.procura.monitoring.ProcessHierarchy.ProcessNode;
import com.procura.monitoring.ProcessInfo;
import com.procura.monitoring.ProcessMonitor;
import com.procura.util.SystemInfo;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * MainFrame: Primary desktop application window for Procura.
 * Hosts 6 tabs: Dashboard, Process Monitor, Process Hierarchy, Job Scheduler, Results, Analytics.
 * Manages periodic background monitoring threads and thread-safe UI telemetry updates.
 */
public class MainFrame extends JFrame {

    private final JTabbedPane tabbedPane;
    private final DashboardPanel dashboardPanel;
    private final ProcessPanel processPanel;
    private final HierarchyPanel hierarchyPanel;
    private final SchedulerPanel schedulerPanel;
    private final ResultsPanel resultsPanel;
    private final AnalyticsPanel analyticsPanel;

    // Status bar labels
    private final JLabel statusOsLabel;
    private final JLabel statusJavaLabel;
    private final JLabel statusRefreshLabel;
    private final JLabel statusMonitoringLabel;

    // Backend components
    private final ProcessMonitor processMonitor;
    private final CpuMonitor cpuMonitor;
    private final MemoryMonitor memoryMonitor;
    private final ProcessHierarchy processHierarchy;
    private final PerformanceAnalyzer performanceAnalyzer;

    // Threading
    private final ScheduledExecutorService schedulerExecutor;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public MainFrame() {
        super("Procura — CPU Monitor & Job Scheduler");

        // 1. Initialize Backend Services
        processMonitor = new ProcessMonitor();
        cpuMonitor = new CpuMonitor();
        memoryMonitor = new MemoryMonitor();
        processHierarchy = new ProcessHierarchy();
        performanceAnalyzer = new PerformanceAnalyzer();

        // 2. Window Configuration
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(1280, 800);
        setMinimumSize(new Dimension(1024, 680));
        setLocationRelativeTo(null);
        getContentPane().setBackground(UITheme.BG_DARK);
        setLayout(new BorderLayout());

        // 3. Initialize Panels
        dashboardPanel = new DashboardPanel();
        processPanel = new ProcessPanel();
        hierarchyPanel = new HierarchyPanel();
        schedulerPanel = new SchedulerPanel();
        resultsPanel = new ResultsPanel();
        analyticsPanel = new AnalyticsPanel();

        // Connect Inter-Panel Dependencies
        analyticsPanel.setPerformanceAnalyzer(performanceAnalyzer);

        // 4. Create and Style Tabs
        tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        tabbedPane.setBackground(UITheme.BG_DARK);
        tabbedPane.setForeground(UITheme.TEXT_MAIN);
        tabbedPane.setFont(UITheme.FONT_BOLD);
        styleTabbedPane(tabbedPane);

        tabbedPane.addTab("  Dashboard  ", dashboardPanel);
        tabbedPane.addTab("  Process Monitor  ", processPanel);
        tabbedPane.addTab("  Process Hierarchy  ", hierarchyPanel);
        tabbedPane.addTab("  Job Scheduler  ", schedulerPanel);
        tabbedPane.addTab("  Results  ", resultsPanel);
        tabbedPane.addTab("  Analytics  ", analyticsPanel);

        // Connect Scheduler to Results and TabbedPane
        schedulerPanel.setResultsTarget(resultsPanel, tabbedPane, dashboardPanel);

        add(tabbedPane, BorderLayout.CENTER);

        // 5. Create Status Bar
        JPanel statusBar = new JPanel(new BorderLayout(15, 0));
        statusBar.setBackground(UITheme.PANEL_BG);
        statusBar.setBorder(new CompoundBorder(new LineBorder(UITheme.BORDER, 1), new EmptyBorder(6, 12, 6, 12)));

        JPanel statusLeft = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        statusLeft.setBackground(UITheme.PANEL_BG);
        statusOsLabel = new JLabel("OS: " + SystemInfo.getOsName() + " (" + cpuMonitor.getCpuCoreCount() + " cores)");
        statusOsLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusOsLabel.setForeground(UITheme.TEXT_MUTED);
        statusLeft.add(statusOsLabel);

        statusJavaLabel = new JLabel("Java: " + SystemInfo.getJavaVersion());
        statusJavaLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusJavaLabel.setForeground(UITheme.TEXT_MUTED);
        statusLeft.add(statusJavaLabel);
        statusBar.add(statusLeft, BorderLayout.WEST);

        JPanel statusRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        statusRight.setBackground(UITheme.PANEL_BG);

        statusMonitoringLabel = new JLabel("● Monitoring Active");
        statusMonitoringLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        statusMonitoringLabel.setForeground(UITheme.SUCCESS);
        statusRight.add(statusMonitoringLabel);

        statusRefreshLabel = new JLabel("Last Refresh: --:--:--");
        statusRefreshLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusRefreshLabel.setForeground(UITheme.TEXT_MUTED);
        statusRight.add(statusRefreshLabel);
        statusBar.add(statusRight, BorderLayout.EAST);

        add(statusBar, BorderLayout.SOUTH);

        // 6. Connect Manual Refresh Handlers
        processPanel.setManualRefreshAction(this::refreshProcessesNow);
        hierarchyPanel.setManualRefreshAction(this::refreshHierarchyNow);

        // 7. Initialize Periodic Monitoring Tasks
        schedulerExecutor = Executors.newScheduledThreadPool(2, r -> {
            Thread t = new Thread(r, "Procura-Monitor-Thread");
            t.setDaemon(true);
            return t;
        });

        startMonitoringTasks();

        // 8. Graceful Shutdown on Window Close
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                shutdownMonitoring();
                dispose();
                System.exit(0);
            }
        });
    }

    private void styleTabbedPane(JTabbedPane pane) {
        pane.setUI(new BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                highlight = UITheme.BORDER;
                lightHighlight = UITheme.BORDER;
                shadow = UITheme.BORDER;
                darkShadow = UITheme.BG_DARK;
                focus = UITheme.ACCENT_PRIMARY;
            }

            @Override
            protected void paintTabBackground(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                if (isSelected) {
                    g2.setColor(UITheme.ACCENT_PRIMARY);
                    g2.fillRect(x, y, w, h);
                } else {
                    g2.setColor(UITheme.PANEL_BG);
                    g2.fillRect(x, y, w, h);
                }
                g2.dispose();
            }

            @Override
            protected void paintTabBorder(Graphics g, int tabPlacement, int tabIndex, int x, int y, int w, int h, boolean isSelected) {
                g.setColor(UITheme.BORDER);
                g.drawRect(x, y, w, h);
            }

            @Override
            protected void paintContentBorder(Graphics g, int tabPlacement, int selectedIndex) {
                g.setColor(UITheme.BORDER);
                g.drawRect(0, 0, tabbedPane.getWidth() - 1, tabbedPane.getHeight() - 1);
            }

            @Override
            protected void paintText(Graphics g, int tabPlacement, Font font, FontMetrics metrics, int tabIndex, String title, Rectangle textRect, boolean isSelected) {
                g.setFont(font);
                g.setColor(isSelected ? Color.WHITE : UITheme.TEXT_MAIN);
                g.drawString(title, textRect.x, textRect.y + metrics.getAscent());
            }
        });
    }

    private void startMonitoringTasks() {
        // Task A: CPU & Memory Telemetry (every 2 seconds)
        schedulerExecutor.scheduleAtFixedRate(() -> {
            try {
                double cpuUsage = cpuMonitor.getCpuUsagePercent();
                double memUsage = memoryMonitor.getMemoryUsagePercent();
                double nowSec = System.currentTimeMillis() / 1000.0;

                // Record analytics samples
                performanceAnalyzer.addCpuSample(nowSec, cpuUsage);
                performanceAnalyzer.addMemorySample(nowSec, memUsage);

                int totalProc = processMonitor.getProcessCount();
                int runningProc = processMonitor.getRunningCount();
                int sleepingProc = processMonitor.getSleepingCount();
                int jobsCount = schedulerPanel.getJobCount();

                // Update Dashboard on EDT
                SwingUtilities.invokeLater(() -> {
                    dashboardPanel.updateStats(cpuUsage, memUsage, totalProc, runningProc, sleepingProc, jobsCount);
                    String timeStr = timeFormat.format(new Date());
                    statusRefreshLabel.setText("Last Refresh: " + timeStr);
                });
            } catch (Exception ex) {
                // Ignore transient /proc read errors gracefully
            }
        }, 0, 2, TimeUnit.SECONDS);

        // Task B: Process List & Hierarchy Telemetry (every 3 seconds)
        schedulerExecutor.scheduleAtFixedRate(() -> {
            try {
                List<ProcessInfo> processes = processMonitor.getProcessList();

                if (processPanel.isAutoRefreshEnabled()) {
                    SwingUtilities.invokeLater(() -> processPanel.updateProcessList(processes));
                }

                if (hierarchyPanel.isAutoRefreshEnabled()) {
                    ProcessNode root = processHierarchy.buildHierarchy(processes);
                    SwingUtilities.invokeLater(() -> hierarchyPanel.updateHierarchy(root));
                }
            } catch (Exception ex) {
                // Ignore transient read errors
            }
        }, 0, 3, TimeUnit.SECONDS);
    }

    private void refreshProcessesNow() {
        schedulerExecutor.execute(() -> {
            try {
                List<ProcessInfo> processes = processMonitor.getProcessList();
                SwingUtilities.invokeLater(() -> processPanel.updateProcessList(processes));
            } catch (Exception ex) {
                // Graceful ignore
            }
        });
    }

    private void refreshHierarchyNow() {
        schedulerExecutor.execute(() -> {
            try {
                List<ProcessInfo> processes = processMonitor.getProcessList();
                ProcessNode root = processHierarchy.buildHierarchy(processes);
                SwingUtilities.invokeLater(() -> hierarchyPanel.updateHierarchy(root));
            } catch (Exception ex) {
                // Graceful ignore
            }
        });
    }

    private void shutdownMonitoring() {
        statusMonitoringLabel.setText("○ Monitoring Stopped");
        statusMonitoringLabel.setForeground(UITheme.TEXT_MUTED);

        if (schedulerExecutor != null && !schedulerExecutor.isShutdown()) {
            schedulerExecutor.shutdown();
            try {
                if (!schedulerExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                    schedulerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                schedulerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Entry point helper for launching standalone UI or testing.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
            } catch (Exception ignored) {}
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
