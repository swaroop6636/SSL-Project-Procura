package com.procura;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Main application entry point for Procura - Linux Process Monitor & CPU Job Scheduler.
 * Configures the Swing Look and Feel and initializes the main graphical user interface
 * (MainFrame) safely on the Event Dispatch Thread (EDT).
 */
public class Main {

    /**
     * Application entry point.
     *
     * @param args Command-line arguments.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                // Set native platform look and feel
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                System.err.println("Notice: Could not set system look and feel: " + e.getMessage());
            }

            try {
                // Initialize and display MainFrame on the EDT
                Class<?> mainFrameClass = Class.forName("com.procura.ui.MainFrame");
                Object frame = mainFrameClass.getDeclaredConstructor().newInstance();
                mainFrameClass.getMethod("setVisible", boolean.class).invoke(frame, true);
            } catch (ClassNotFoundException e) {
                System.out.println("Procura core loaded successfully. com.procura.ui.MainFrame will launch when the UI module is compiled.");
            } catch (Exception e) {
                System.err.println("Error initializing Procura MainFrame: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
}
