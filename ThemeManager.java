package util;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.prefs.Preferences;

/**
 * Manages application-wide theme settings and color schemes
 */
public class ThemeManager {

    // Theme types
    public enum Theme {
        LIGHT, DARK
    }

    // Preference keys
    private static final String PREF_THEME = "theme";

    // Singleton instance
    private static ThemeManager instance;

    // Current theme
    private Theme currentTheme = Theme.LIGHT;

    // Color maps for different themes
    private final Map<String, Color> lightColors = new HashMap<>();
    private final Map<String, Color> darkColors = new HashMap<>();

    // Preferences storage
    private final Preferences prefs = Preferences.userNodeForPackage(ThemeManager.class);

    // Theme change listeners
    private final List<ActionListener> themeChangeListeners = new ArrayList<>();

    private ThemeManager() {
        // Initialize color schemes
        setupLightTheme();
        setupDarkTheme();

        // Load saved theme preference
        String savedTheme = prefs.get(PREF_THEME, Theme.LIGHT.name());
        try {
            currentTheme = Theme.valueOf(savedTheme);
        } catch (IllegalArgumentException e) {
            currentTheme = Theme.LIGHT;
        }
    }

    public static synchronized ThemeManager getInstance() {
        if (instance == null) {
            instance = new ThemeManager();
        }
        return instance;
    }

    private void setupLightTheme() {
        // Background colors
        lightColors.put("background", new Color(173, 216, 230)); // Light blue
        lightColors.put("panelBackground", new Color(240, 240, 250)); // Very light lavender
        lightColors.put("cardBackground", Color.WHITE);

        // Text colors
        lightColors.put("text", Color.BLACK);
        lightColors.put("secondaryText", Color.DARK_GRAY);

        // Accent colors
        lightColors.put("accent", new Color(199, 21, 133)); // Deep pink
        lightColors.put("buttonBackground", new Color(147, 112, 219)); // Medium purple
        lightColors.put("buttonText", Color.WHITE);

        // Ensure proper contrast for specific components
        lightColors.put("study.text", new Color(46, 139, 87)); // Sea green
        lightColors.put("break.text", new Color(70, 130, 180)); // Steel blue
        lightColors.put("button.background", new Color(147, 112, 219)); // Medium purple
        lightColors.put("button.foreground", Color.WHITE);
        lightColors.put("panelForeground", Color.BLACK);

        // Progress colors
        lightColors.put("progress1", new Color(75, 0, 130)); // Indigo
        lightColors.put("progress2", new Color(46, 139, 87)); // Sea green
    }

    private void setupDarkTheme() {
        // Background colors
        darkColors.put("background", new Color(45, 45, 45)); // Dark gray
        darkColors.put("panelBackground", new Color(60, 60, 60)); // Medium dark gray
        darkColors.put("cardBackground", new Color(75, 75, 75)); // Medium gray

        // Text colors
        darkColors.put("text", Color.WHITE);
        darkColors.put("secondaryText", new Color(200, 200, 200)); // Light gray

        // Accent colors
        darkColors.put("accent", new Color(255, 105, 180)); // Hot pink (brighter for dark mode)
        darkColors.put("buttonBackground", new Color(138, 43, 226)); // Blue violet
        darkColors.put("buttonText", Color.WHITE);

        // Ensure proper contrast for specific components
        darkColors.put("study.text", new Color(50, 205, 50)); // Bright lime green
        darkColors.put("break.text", new Color(135, 206, 250)); // Light sky blue
        darkColors.put("button.background", new Color(138, 43, 226)); // Blue violet
        darkColors.put("button.foreground", Color.WHITE);
        darkColors.put("panelForeground", Color.WHITE);

        // Progress colors
        darkColors.put("progress1", new Color(123, 104, 238)); // Medium slate blue
        darkColors.put("progress2", new Color(50, 205, 50)); // Lime green
    }

    public Color getColor(String colorKey) {
        Map<String, Color> currentColors = (currentTheme == Theme.LIGHT) ? lightColors : darkColors;
        Color color = currentColors.get(colorKey);
        return (color != null) ? color : Color.GRAY; // Default fallback color
    }

    public void setTheme(Theme theme) {
        this.currentTheme = theme;
        prefs.put(PREF_THEME, theme.name());

        // Update UI manager defaults for standard Swing components
        updateUIManagerDefaults();

        // Notify listeners about the theme change
        notifyThemeChanged();
    }

    public Theme getCurrentTheme() {
        return currentTheme;
    }

    public boolean isDarkTheme() {
        return currentTheme == Theme.DARK;
    }

    private void updateUIManagerDefaults() {
        if (currentTheme == Theme.DARK) {
            // Set dark theme defaults
            UIManager.put("Panel.background", getColor("panelBackground"));
            UIManager.put("OptionPane.background", getColor("panelBackground"));
            UIManager.put("TextField.background", getColor("cardBackground"));
            UIManager.put("ComboBox.background", getColor("cardBackground"));
            UIManager.put("TextArea.background", getColor("cardBackground"));
            UIManager.put("Button.background", getColor("buttonBackground"));

            UIManager.put("Label.foreground", getColor("text"));
            UIManager.put("TextField.foreground", getColor("text"));
            UIManager.put("TextArea.foreground", getColor("text"));
            UIManager.put("ComboBox.foreground", getColor("text"));
            UIManager.put("Button.foreground", getColor("buttonText"));

            UIManager.put("TabbedPane.background", getColor("background"));
            UIManager.put("TabbedPane.foreground", getColor("text"));
            UIManager.put("TabbedPane.selected", getColor("cardBackground"));
        } else {
            // Reset to light theme defaults
            UIManager.put("Panel.background", getColor("panelBackground"));
            UIManager.put("OptionPane.background", UIManager.getColor("Panel.background"));
            UIManager.put("TextField.background", getColor("cardBackground"));
            UIManager.put("ComboBox.background", getColor("cardBackground"));
            UIManager.put("TextArea.background", getColor("cardBackground"));
            UIManager.put("Button.background", getColor("buttonBackground"));

            UIManager.put("Label.foreground", getColor("text"));
            UIManager.put("TextField.foreground", getColor("text"));
            UIManager.put("TextArea.foreground", getColor("text"));
            UIManager.put("ComboBox.foreground", getColor("text"));
            UIManager.put("Button.foreground", getColor("buttonText"));

            UIManager.put("TabbedPane.background", getColor("background"));
            UIManager.put("TabbedPane.foreground", getColor("text"));
            UIManager.put("TabbedPane.selected", getColor("cardBackground"));
        }
    }

    /**
     * Apply the current theme to a JFrame
     */
    public void applyTheme(JFrame frame) {
        frame.getContentPane().setBackground(getColor("background"));
        SwingUtilities.updateComponentTreeUI(frame);
    }

    /**
     * Add a listener that will be notified when the theme changes
     */
    public void addThemeChangeListener(ActionListener listener) {
        themeChangeListeners.add(listener);
    }

    /**
     * Remove a theme change listener
     */
    public void removeThemeChangeListener(ActionListener listener) {
        themeChangeListeners.remove(listener);
    }

    /**
     * Notify all registered listeners about a theme change
     */
    private void notifyThemeChanged() {
        for (ActionListener listener : themeChangeListeners) {
            listener.actionPerformed(new java.awt.event.ActionEvent(this, 0, "themeChanged"));
        }
    }
}
