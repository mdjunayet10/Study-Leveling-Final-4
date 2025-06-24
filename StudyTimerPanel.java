package ui;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import util.ThemeManager;

/**
 * A Pomodoro timer panel that can be added to study screens
 */
public class StudyTimerPanel extends JPanel {
    private static final int DEFAULT_STUDY_MINUTES = 25;
    private static final int DEFAULT_BREAK_MINUTES = 5;
    private static final int DEFAULT_LONG_BREAK_MINUTES = 15;

    private final Timer timer;
    private final JLabel timeLabel;
    private final JButton startPauseButton;
    private final JButton resetButton;
    private final JButton skipButton;
    private final JLabel statusLabel;
    private final JLabel cycleCountLabel;

    private int secondsLeft;
    private boolean isRunning = false;
    private boolean isStudySession = true;
    private int cycleCount = 0;
    private int studyMinutes; // Not final so it can be changed
    private int breakMinutes; // Not final so it can be changed
    private int longBreakMinutes; // Not final so it can be changed
    private int pomodoroCount = 0;

    // Use ThemeManager to get theme-aware colors
    private final ThemeManager themeManager = ThemeManager.getInstance();
    private Color accentColor;
    private Color studyColor;
    private Color breakColor;
    private Color textColor;
    private Color backgroundColor;

    public StudyTimerPanel() {
        this(DEFAULT_STUDY_MINUTES, DEFAULT_BREAK_MINUTES, DEFAULT_LONG_BREAK_MINUTES);
    }

    public StudyTimerPanel(int studyMinutes, int breakMinutes, int longBreakMinutes) {
        this.studyMinutes = studyMinutes;
        this.breakMinutes = breakMinutes;
        this.longBreakMinutes = longBreakMinutes;
        this.secondsLeft = studyMinutes * 60;

        // Initialize theme-aware colors
        updateColors();

        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(accentColor, 2),
                "⏱️ Pomodoro Timer",
                TitledBorder.LEFT,
                TitledBorder.TOP,
                new Font("Monospaced", Font.BOLD, 14),
                accentColor
            ),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        setBackground(backgroundColor);

        // Create timer components
        timeLabel = new JLabel(formatTime(secondsLeft));
        timeLabel.setFont(new Font("Monospaced", Font.BOLD, 36));
        timeLabel.setHorizontalAlignment(SwingConstants.CENTER);
        timeLabel.setForeground(studyColor);

        statusLabel = new JLabel("Study Session");
        statusLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setForeground(studyColor);

        cycleCountLabel = new JLabel("Pomodoros: 0");
        cycleCountLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));
        cycleCountLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Set up the timer
        timer = new Timer(1000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                secondsLeft--;
                timeLabel.setText(formatTime(secondsLeft));

                if (secondsLeft <= 0) {
                    timer.stop();
                    isRunning = false;
                    playSound();

                    if (isStudySession) {
                        pomodoroCount++;
                        cycleCountLabel.setText("Pomodoros: " + pomodoroCount);

                        // After 4 pomodoros, take a long break
                        if (pomodoroCount % 4 == 0) {
                            isStudySession = false;
                            secondsLeft = longBreakMinutes * 60;
                            statusLabel.setText("Long Break - Relax!");
                            statusLabel.setForeground(breakColor);
                            timeLabel.setForeground(breakColor);
                            JOptionPane.showMessageDialog(
                                StudyTimerPanel.this,
                                "Study session complete! Take a " + longBreakMinutes + " minute long break.",
                                "Long Break Time",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            isStudySession = false;
                            secondsLeft = breakMinutes * 60;
                            statusLabel.setText("Break Time - Relax!");
                            statusLabel.setForeground(breakColor);
                            timeLabel.setForeground(breakColor);
                            JOptionPane.showMessageDialog(
                                StudyTimerPanel.this,
                                "Study session complete! Take a " + breakMinutes + " minute break.",
                                "Break Time",
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    } else {
                        isStudySession = true;
                        secondsLeft = studyMinutes * 60;
                        statusLabel.setText("Study Session");
                        statusLabel.setForeground(studyColor);
                        timeLabel.setForeground(studyColor);
                        JOptionPane.showMessageDialog(
                            StudyTimerPanel.this,
                            "Break time is over! Ready for another " + studyMinutes + " minute study session?",
                            "Back to Study",
                            JOptionPane.INFORMATION_MESSAGE
                        );
                    }

                    timeLabel.setText(formatTime(secondsLeft));
                    startPauseButton.setText("▶ Start");
                }
            }
        });

        // Create buttons with fixed-width and proper labels
        startPauseButton = createButton("▶ Start", 100, 30);
        startPauseButton.addActionListener(e -> {
            if (isRunning) {
                timer.stop();
                isRunning = false;
                startPauseButton.setText("▶ Resume");
            } else {
                timer.start();
                isRunning = true;
                startPauseButton.setText("⏸ Pause");
            }
        });

        resetButton = createButton("↻ Reset", 100, 30);
        resetButton.addActionListener(e -> {
            timer.stop();
            isRunning = false;
            if (isStudySession) {
                secondsLeft = studyMinutes * 60;
            } else {
                secondsLeft = breakMinutes * 60;
            }
            timeLabel.setText(formatTime(secondsLeft));
            startPauseButton.setText("▶ Start");
        });

        skipButton = createButton("⏭ Skip", 100, 30);
        skipButton.addActionListener(e -> {
            timer.stop();
            isRunning = false;

            if (isStudySession) {
                // Skip to break
                pomodoroCount++;
                cycleCountLabel.setText("Pomodoros: " + pomodoroCount);

                if (pomodoroCount % 4 == 0) {
                    secondsLeft = longBreakMinutes * 60;
                    statusLabel.setText("Long Break - Relax!");
                } else {
                    secondsLeft = breakMinutes * 60;
                    statusLabel.setText("Break Time - Relax!");
                }
                statusLabel.setForeground(breakColor);
                timeLabel.setForeground(breakColor);
                isStudySession = false;
            } else {
                // Skip to study
                secondsLeft = studyMinutes * 60;
                statusLabel.setText("Study Session");
                statusLabel.setForeground(studyColor);
                timeLabel.setForeground(studyColor);
                isStudySession = true;
            }

            timeLabel.setText(formatTime(secondsLeft));
            startPauseButton.setText("▶ Start");
        });

        // Settings section
        JPanel settingsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        settingsPanel.setOpaque(false);

        String[] presets = {"25/5", "50/10", "45/15"};
        JComboBox<String> presetCombo = new JComboBox<>(presets);
        presetCombo.setFont(new Font("Monospaced", Font.PLAIN, 14));
        presetCombo.addActionListener(e -> {
            timer.stop();
            isRunning = false;

            String selectedPreset = (String) presetCombo.getSelectedItem();
            if (selectedPreset.equals("25/5")) {
                updateTimerSettings(25, 5, 15);
            } else if (selectedPreset.equals("50/10")) {
                updateTimerSettings(50, 10, 20);
            } else if (selectedPreset.equals("45/15")) {
                updateTimerSettings(45, 15, 30);
            }

            startPauseButton.setText("▶ Start");
        });

        JLabel presetLabel = new JLabel("Preset: ");
        presetLabel.setFont(new Font("Monospaced", Font.PLAIN, 14));

        settingsPanel.add(presetLabel);
        settingsPanel.add(presetCombo);

        // Arrange components
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setOpaque(false);
        topPanel.add(statusLabel, BorderLayout.NORTH);
        topPanel.add(timeLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(startPauseButton);
        buttonPanel.add(resetButton);
        buttonPanel.add(skipButton);

        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setOpaque(false);
        bottomPanel.add(buttonPanel, BorderLayout.NORTH);
        bottomPanel.add(cycleCountLabel, BorderLayout.CENTER);
        bottomPanel.add(settingsPanel, BorderLayout.SOUTH);

        add(topPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    // Helper method to create consistent buttons with fixed size
    private JButton createButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        button.setFont(new Font("Monospaced", Font.BOLD, 14));
        button.setFocusPainted(false);
        button.setBackground(themeManager.getColor("button.background")); // Use theme color
        button.setForeground(themeManager.getColor("button.foreground")); // Use theme color
        button.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        return button;
    }

    private void updateTimerSettings(int newStudyMinutes, int newBreakMinutes, int newLongBreakMinutes) {
        // Save new settings but don't restart the current timer unless it's reset
        this.studyMinutes = newStudyMinutes;
        this.breakMinutes = newBreakMinutes;
        this.longBreakMinutes = newLongBreakMinutes;

        // Update current timer if not running
        if (!isRunning) {
            if (isStudySession) {
                secondsLeft = studyMinutes * 60;
            } else {
                secondsLeft = breakMinutes * 60;
            }
            timeLabel.setText(formatTime(secondsLeft));
        }
    }

    private String formatTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        DecimalFormat format = new DecimalFormat("00");
        return format.format(minutes) + ":" + format.format(seconds);
    }

    private void playSound() {
        Toolkit.getDefaultToolkit().beep();
    }

    /**
     * Stops the timer when the containing window is closed
     */
    public void stopTimer() {
        if (timer.isRunning()) {
            timer.stop();
            isRunning = false;
        }
    }

    /**
     * Updates colors based on the current theme
     */
    private void updateColors() {
        // Get theme-specific colors with better contrast
        accentColor = themeManager.isDarkTheme() ?
            new Color(255, 105, 180) : // Hot pink for dark mode
            new Color(199, 21, 133);   // Deep pink for light mode

        studyColor = themeManager.isDarkTheme() ?
            new Color(50, 205, 50) :   // Bright lime green for dark mode
            new Color(46, 139, 87);    // Sea green for light mode

        breakColor = themeManager.isDarkTheme() ?
            new Color(135, 206, 250) : // Light sky blue for dark mode
            new Color(70, 130, 180);   // Steel blue for light mode

        textColor = themeManager.getColor("text");
        backgroundColor = themeManager.getColor("panelBackground");

        // Apply colors to components if they exist
        if (isDisplayable()) {
            setBackground(backgroundColor);

            if (timeLabel != null) {
                timeLabel.setForeground(isStudySession ? studyColor : breakColor);
            }

            if (statusLabel != null) {
                statusLabel.setForeground(isStudySession ? studyColor : breakColor);
            }

            if (cycleCountLabel != null) {
                cycleCountLabel.setForeground(textColor);
            }

            // Update border with new accent color
            setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(
                    BorderFactory.createLineBorder(accentColor, 2),
                    "⏱️ Pomodoro Timer",
                    TitledBorder.LEFT,
                    TitledBorder.TOP,
                    new Font("Monospaced", Font.BOLD, 14),
                    accentColor
                ),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
            ));

            // Update buttons
            if (startPauseButton != null && resetButton != null && skipButton != null) {
                updateButtonColors(startPauseButton);
                updateButtonColors(resetButton);
                updateButtonColors(skipButton);
            }

            repaint();
        }
    }

    private void updateButtonColors(JButton button) {
        Color buttonBg = themeManager.getColor("buttonBackground");
        Color buttonText = themeManager.getColor("buttonText");
        Color borderColor = themeManager.isDarkTheme() ? Color.GRAY : Color.BLACK;

        button.setBackground(buttonBg);
        button.setForeground(buttonText);
        button.setBorder(BorderFactory.createLineBorder(borderColor, 1));
    }

    /**
     * Override to update colors when added to container
     */
    @Override
    public void addNotify() {
        super.addNotify();
        updateColors();
    }

    /**
     * Update the panel when theme changes
     * Call this method from parent components when theme changes
     */
    public void refreshTheme() {
        updateColors();
    }
}
