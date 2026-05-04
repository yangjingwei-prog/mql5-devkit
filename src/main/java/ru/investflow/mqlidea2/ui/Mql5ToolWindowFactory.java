package ru.investflow.mqlidea2.ui;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionToolbar;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;

/**
 * MQL5 Build Log 工具窗口
 * 显示编译日志和输出
 */
public class Mql5ToolWindowFactory implements ToolWindowFactory {

    @Override
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        JPanel panel = new JPanel(new BorderLayout());

        JTextArea logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, logArea.getFont().getSize()));
        logArea.setText("MQL5 Build Log\nReady.\n");

        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        // IntelliJ-style vertical icon toolbar on the left sidebar
        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(new CopyLogAction(logArea));
        actionGroup.add(new ClearLogAction(logArea));

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(
                "MQL5BuildLog", actionGroup, false);
        toolbar.setTargetComponent(panel);
        panel.add(toolbar.getComponent(), BorderLayout.WEST);

        // Register log area with the build service
        Mql5BuildLogService.getInstance(project).setLogArea(logArea);

        ContentFactory factory = ContentFactory.getInstance();
        Content content = factory.createContent(panel, "Build Log", false);
        toolWindow.getContentManager().addContent(content);
    }

    @Override
    public boolean shouldBeAvailable(@NotNull Project project) {
        return true;
    }

    private static class ClearLogAction extends AnAction implements DumbAware {
        private final JTextArea logArea;

        ClearLogAction(JTextArea logArea) {
            super("Clear", "Clear build log", AllIcons.Actions.GC);
            this.logArea = logArea;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            logArea.setText("");
        }
    }

    private static class CopyLogAction extends AnAction implements DumbAware {
        private final JTextArea logArea;

        CopyLogAction(JTextArea logArea) {
            super("Copy All", "Copy build log to clipboard", AllIcons.Actions.Copy);
            this.logArea = logArea;
        }

        @Override
        public void actionPerformed(@NotNull AnActionEvent e) {
            String text = logArea.getText();
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        }
    }
}
