package com.procura.ui;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;

/**
 * Common theme constants and UI styling helpers for Procura dark theme.
 */
public final class UITheme {

    public static final Color BG_DARK = new Color(30, 30, 30);
    public static final Color PANEL_BG = new Color(40, 40, 40);
    public static final Color CARD_BG = new Color(50, 50, 50);
    public static final Color TEXT_MAIN = new Color(220, 220, 220);
    public static final Color TEXT_MUTED = new Color(160, 160, 160);
    public static final Color ACCENT_PRIMARY = new Color(70, 130, 230);
    public static final Color ACCENT_HOVER = new Color(90, 150, 250);
    public static final Color SUCCESS = new Color(76, 175, 80);
    public static final Color WARNING = new Color(255, 167, 38);
    public static final Color ERROR = new Color(244, 67, 54);
    public static final Color TABLE_HEADER = new Color(60, 60, 60);
    public static final Color TABLE_ROW_ALT = new Color(45, 45, 45);
    public static final Color BORDER = new Color(60, 60, 60);

    public static final Font FONT_BODY = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD = new Font("Segoe UI", Font.BOLD, 13);
    public static final Font FONT_HEADER = new Font("Segoe UI", Font.BOLD, 18);
    public static final Font FONT_SUBHEADER = new Font("Segoe UI", Font.BOLD, 15);
    public static final Font FONT_CARD_VAL = new Font("Segoe UI", Font.BOLD, 24);
    public static final Font FONT_MONO = new Font("Consolas", Font.PLAIN, 12);

    private UITheme() {}

    public static Border createCardBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1, true),
                new EmptyBorder(12, 14, 12, 14)
        );
    }

    public static Border createSectionBorder() {
        return new CompoundBorder(
                new LineBorder(BORDER, 1),
                new EmptyBorder(10, 12, 10, 12)
        );
    }

    public static JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(ACCENT_PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(ACCENT_PRIMARY.darker(), 1), new EmptyBorder(6, 14, 6, 14)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(ACCENT_HOVER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(ACCENT_PRIMARY);
            }
        });
        return btn;
    }

    public static JButton createSecondaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BODY);
        btn.setForeground(TEXT_MAIN);
        btn.setBackground(CARD_BG);
        btn.setFocusPainted(false);
        btn.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(6, 12, 6, 12)));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(TABLE_HEADER);
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (btn.isEnabled()) btn.setBackground(CARD_BG);
            }
        });
        return btn;
    }

    public static JTextField createTextField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(FONT_BODY);
        tf.setBackground(PANEL_BG);
        tf.setForeground(TEXT_MAIN);
        tf.setCaretColor(TEXT_MAIN);
        tf.setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(5, 8, 5, 8)));
        return tf;
    }

    public static JSpinner createSpinner(SpinnerModel model) {
        JSpinner spinner = new JSpinner(model);
        spinner.setFont(FONT_BODY);
        spinner.setBackground(PANEL_BG);
        spinner.setForeground(TEXT_MAIN);
        JComponent editor = spinner.getEditor();
        if (editor instanceof JSpinner.DefaultEditor) {
            JTextField tf = ((JSpinner.DefaultEditor) editor).getTextField();
            tf.setBackground(PANEL_BG);
            tf.setForeground(TEXT_MAIN);
            tf.setCaretColor(TEXT_MAIN);
            tf.setBorder(new EmptyBorder(4, 6, 4, 6));
        }
        spinner.setBorder(new LineBorder(BORDER, 1));
        return spinner;
    }

    public static <T> JComboBox<T> createComboBox(T[] items) {
        JComboBox<T> combo = new JComboBox<>(items);
        combo.setFont(FONT_BODY);
        combo.setBackground(PANEL_BG);
        combo.setForeground(TEXT_MAIN);
        combo.setFocusable(false);
        combo.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                setBackground(isSelected ? ACCENT_PRIMARY : PANEL_BG);
                setForeground(TEXT_MAIN);
                setBorder(new EmptyBorder(4, 8, 4, 8));
                return this;
            }
        });
        return combo;
    }

    public static JLabel createHeaderLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(FONT_HEADER);
        label.setForeground(TEXT_MAIN);
        label.setBorder(new EmptyBorder(6, 4, 10, 4));
        return label;
    }

    public static void styleTable(JTable table) {
        table.setBackground(PANEL_BG);
        table.setForeground(TEXT_MAIN);
        table.setFont(FONT_BODY);
        table.setGridColor(BORDER);
        table.setRowHeight(26);
        table.setSelectionBackground(new Color(50, 80, 130));
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setBackground(TABLE_HEADER);
        header.setForeground(TEXT_MAIN);
        header.setFont(FONT_BOLD);
        header.setBorder(new LineBorder(BORDER, 1));
        header.setReorderingAllowed(false);
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object val, boolean isSel, boolean hasFocus, int r, int c) {
                super.getTableCellRendererComponent(tbl, val, isSel, hasFocus, r, c);
                setBackground(TABLE_HEADER);
                setForeground(Color.WHITE);
                setFont(FONT_BOLD);
                setHorizontalAlignment(CENTER);
                setBorder(new CompoundBorder(new LineBorder(BORDER, 1), new EmptyBorder(6, 6, 6, 6)));
                return this;
            }
        });
    }

    public static void styleScrollPane(JScrollPane sp) {
        sp.setBackground(BG_DARK);
        sp.getViewport().setBackground(PANEL_BG);
        sp.setBorder(new LineBorder(BORDER, 1));
        sp.getVerticalScrollBar().setBackground(PANEL_BG);
        sp.getHorizontalScrollBar().setBackground(PANEL_BG);
    }
}
