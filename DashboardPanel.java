package com.procura.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.text.DefaultCaret;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * DashboardPanel: Overview dashboard showing 6 key metric cards and an activity log.
 */
public class DashboardPanel extends JPanel {

    private final JLabel cpuValueLabel;
    private final JProgressBar cpuProgressBar;

    private final JLabel memoryValueLabel;
    private final JProgressBar memoryProgressBar;

    private final JLabel totalProcLabel;
    private final JLabel runningProcLabel;
    private final JLabel sleepingProcLabel;
    private final JLabel submittedJobsLabel;

    private final JTextArea activityTextArea;
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss");

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBackground(UITheme.BG_DARK);
        setBorder(new EmptyBorder(16, 16, 16, 16));

        // Top Panel: Header & 6 Stat Cards
        JPanel topContainer = new JPanel(new BorderLayout(0, 14));
        topContainer.setBackground(UITheme.BG_DARK);

        // Header Title
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(UITheme.BG_DARK);
        JLabel titleLabel = UITheme.createHeaderLabel("System Dashboard");
        JLabel subLabel = new JLabel("Real-time Linux process telemetry & CPU scheduler status");
        subLabel.setFont(UITheme.FONT_BODY);
        subLabel.setForeground(UITheme.TEXT_MUTED);
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        headerPanel.add(subLabel, BorderLayout.SOUTH);
        topContainer.add(headerPanel, BorderLayout.NORTH);

        // 6 Stat Cards in a Grid
        JPanel cardsGrid = new JPanel(new GridLayout(1, 6, 12, 12));
        cardsGrid.setBackground(UITheme.BG_DARK);

        // 1. CPU Card
        JPanel cpuCard = createCardPanel("⚡ CPU Usage");
        cpuValueLabel = createStatValueLabel("0.0%");
        cpuProgressBar = createMiniProgressBar();
        cpuCard.add(cpuValueLabel, BorderLayout.CENTER);
        JPanel cpuBottom = new JPanel(new BorderLayout(0, 4));
        cpuBottom.setBackground(UITheme.CARD_BG);
        cpuBottom.add(cpuProgressBar, BorderLayout.CENTER);
        JLabel cpuSub = new JLabel("Processor Load");
        cpuSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        cpuSub.setForeground(UITheme.TEXT_MUTED);
        cpuBottom.add(cpuSub, BorderLayout.SOUTH);
        cpuCard.add(cpuBottom, BorderLayout.SOUTH);
        cardsGrid.add(cpuCard);

        // 2. Memory Card
        JPanel memCard = createCardPanel("💾 Memory");
        memoryValueLabel = createStatValueLabel("0.0%");
        memoryProgressBar = createMiniProgressBar();
        memCard.add(memoryValueLabel, BorderLayout.CENTER);
        JPanel memBottom = new JPanel(new BorderLayout(0, 4));
        memBottom.setBackground(UITheme.CARD_BG);
        memBottom.add(memoryProgressBar, BorderLayout.CENTER);
        JLabel memSub = new JLabel("RAM Utilization");
        memSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        memSub.setForeground(UITheme.TEXT_MUTED);
        memBottom.add(memSub, BorderLayout.SOUTH);
        memCard.add(memBottom, BorderLayout.SOUTH);
        cardsGrid.add(memCard);

        // 3. Total Processes Card
        JPanel totalCard = createCardPanel("📊 Processes");
        totalProcLabel = createStatValueLabel("0");
        totalCard.add(totalProcLabel, BorderLayout.CENTER);
        totalCard.add(createCardFooter("Total in /proc"), BorderLayout.SOUTH);
        cardsGrid.add(totalCard);

        // 4. Running Processes Card
        JPanel runningCard = createCardPanel("▶ Running");
        runningProcLabel = createStatValueLabel("0");
        runningProcLabel.setForeground(UITheme.SUCCESS);
        runningCard.add(runningProcLabel, BorderLayout.CENTER);
        runningCard.add(createCardFooter("Active in CPU"), BorderLayout.SOUTH);
        cardsGrid.add(runningCard);

        // 5. Sleeping Processes Card
        JPanel sleepingCard = createCardPanel("💤 Sleeping");
        sleepingProcLabel = createStatValueLabel("0");
        sleepingProcLabel.setForeground(UITheme.WARNING);
        sleepingCard.add(sleepingProcLabel, BorderLayout.CENTER);
        sleepingCard.add(createCardFooter("Waiting / Idle"), BorderLayout.SOUTH);
        cardsGrid.add(sleepingCard);

        // 6. Submitted Jobs Card
        JPanel jobsCard = createCardPanel("📋 Jobs");
        submittedJobsLabel = createStatValueLabel("0");
        submittedJobsLabel.setForeground(UITheme.ACCENT_PRIMARY);
        jobsCard.add(submittedJobsLabel, BorderLayout.CENTER);
        jobsCard.add(createCardFooter("In Scheduler Queue"), BorderLayout.SOUTH);
        cardsGrid.add(jobsCard);

        topContainer.add(cardsGrid, BorderLayout.CENTER);
        add(topContainer, BorderLayout.NORTH);

        // Center / Bottom Panel: Activity Log
        JPanel activityPanel = new JPanel(new BorderLayout(0, 8));
        activityPanel.setBackground(UITheme.PANEL_BG);
        activityPanel.setBorder(UITheme.createSectionBorder());

        JPanel logHeader = new JPanel(new BorderLayout());
        logHeader.setBackground(UITheme.PANEL_BG);
        JLabel logTitle = new JLabel("Recent Activity & Event Log");
        logTitle.setFont(UITheme.FONT_SUBHEADER);
        logTitle.setForeground(UITheme.TEXT_MAIN);

        JButton clearBtn = UITheme.createSecondaryButton("Clear Log");
        clearBtn.addActionListener(e -> activityTextArea.setText(""));

        logHeader.add(logTitle, BorderLayout.WEST);
        logHeader.add(clearBtn, BorderLayout.EAST);
        activityPanel.add(logHeader, BorderLayout.NORTH);

        activityTextArea = new JTextArea();
        activityTextArea.setEditable(false);
        activityTextArea.setBackground(UITheme.BG_DARK);
        activityTextArea.setForeground(UITheme.TEXT_MAIN);
        activityTextArea.setFont(UITheme.FONT_MONO);
        activityTextArea.setLineWrap(true);
        activityTextArea.setWrapStyleWord(true);
        activityTextArea.setBorder(new EmptyBorder(8, 8, 8, 8));

        DefaultCaret caret = (DefaultCaret) activityTextArea.getCaret();
        caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

        JScrollPane logScroll = new JScrollPane(activityTextArea);
        UITheme.styleScrollPane(logScroll);
        activityPanel.add(logScroll, BorderLayout.CENTER);

        add(activityPanel, BorderLayout.CENTER);

        // Initial welcome log entry
        addActivity("Procura system monitor initialized successfully.");
    }

    private JPanel createCardPanel(String iconAndTitle) {
        JPanel card = new JPanel(new BorderLayout(0, 8));
        card.setBackground(UITheme.CARD_BG);
        card.setBorder(UITheme.createCardBorder());

        JLabel header = new JLabel(iconAndTitle);
        header.setFont(UITheme.FONT_BOLD);
        header.setForeground(UITheme.TEXT_MUTED);
        card.add(header, BorderLayout.NORTH);

        return card;
    }

    private JLabel createStatValueLabel(String text) {
        JLabel label = new JLabel(text, SwingConstants.LEFT);
        label.setFont(UITheme.FONT_CARD_VAL);
        label.setForeground(UITheme.TEXT_MAIN);
        return label;
    }

    private JLabel createCardFooter(String text) {
        JLabel sub = new JLabel(text);
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        sub.setForeground(UITheme.TEXT_MUTED);
        return sub;
    }

    private JProgressBar createMiniProgressBar() {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setPreferredSize(new Dimension(80, 6));
        bar.setBackground(UITheme.PANEL_BG);
        bar.setForeground(UITheme.ACCENT_PRIMARY);
        bar.setBorder(new LineBorder(UITheme.BORDER, 1));
        return bar;
    }

    /**
     * Updates the stat cards with latest telemetry data.
     */
    public void updateStats(double cpu, double memory, int total, int running, int sleeping, int jobs) {
        SwingUtilities.invokeLater(() -> {
            cpuValueLabel.setText(String.format("%.1f%%", Math.max(0.0, cpu)));
            int cpuInt = (int) Math.min(100, Math.max(0, Math.round(cpu)));
            cpuProgressBar.setValue(cpuInt);
            if (cpuInt >= 80) {
                cpuProgressBar.setForeground(UITheme.ERROR);
            } else if (cpuInt >= 50) {
                cpuProgressBar.setForeground(UITheme.WARNING);
            } else {
                cpuProgressBar.setForeground(UITheme.ACCENT_PRIMARY);
            }

            memoryValueLabel.setText(String.format("%.1f%%", Math.max(0.0, memory)));
            int memInt = (int) Math.min(100, Math.max(0, Math.round(memory)));
            memoryProgressBar.setValue(memInt);
            if (memInt >= 85) {
                memoryProgressBar.setForeground(UITheme.ERROR);
            } else if (memInt >= 60) {
                memoryProgressBar.setForeground(UITheme.WARNING);
            } else {
                memoryProgressBar.setForeground(UITheme.SUCCESS);
            }

            totalProcLabel.setText(String.valueOf(Math.max(0, total)));
            runningProcLabel.setText(String.valueOf(Math.max(0, running)));
            sleepingProcLabel.setText(String.valueOf(Math.max(0, sleeping)));
            submittedJobsLabel.setText(String.valueOf(Math.max(0, jobs)));
        });
    }

    /**
     * Appends an event or log message to the activity console.
     */
    public void addActivity(String message) {
        SwingUtilities.invokeLater(() -> {
            String time = timeFormat.format(new Date());
            activityTextArea.append("[" + time + "] " + message + "\n");
        });
    }
}
