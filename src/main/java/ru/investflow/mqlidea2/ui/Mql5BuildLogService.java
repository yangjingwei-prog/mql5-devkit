package ru.investflow.mqlidea2.ui;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowManager;
import org.jetbrains.annotations.NotNull;

import javax.swing.JTextArea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Project-level service that manages the MQL5 Build Log tool window content.
 * Provides methods to append log messages and auto-scroll.
 */
public class Mql5BuildLogService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm:ss");

    private final Project project;
    private JTextArea logArea;

    public Mql5BuildLogService(@NotNull Project project) {
        this.project = project;
    }

    public void setLogArea(@NotNull JTextArea logArea) {
        this.logArea = logArea;
    }

    public void clear() {
        if (logArea == null) return;
        ApplicationManager.getApplication().invokeLater(() -> {
            logArea.setText("");
        });
    }

    public void append(@NotNull String text) {
        if (logArea == null) return;
        ApplicationManager.getApplication().invokeLater(() -> {
            logArea.append(text);
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    /**
     * Append a timestamped line to the log.
     */
    public void appendLine(@NotNull String line) {
        append(line + "\n");
    }

    /**
     * Start a new build session: clear log, print header, and activate the tool window.
     */
    public void startBuild(@NotNull String fileName) {
        clear();
        appendLine("=== MQL5 Build: " + fileName + " [" + LocalDateTime.now().format(TIME_FMT) + "] ===");
        activateToolWindow();
    }

    /**
     * Append build summary line.
     */
    public void appendSummary(@NotNull String summary) {
        appendLine("--- " + summary + " ---");
    }

    private void activateToolWindow() {
        ToolWindow tw = ToolWindowManager.getInstance(project).getToolWindow("MQL5 Build");
        if (tw != null) {
            tw.activate(null);
        }
    }

    public static @NotNull Mql5BuildLogService getInstance(@NotNull Project project) {
        return project.getService(Mql5BuildLogService.class);
    }
}
