package ru.investflow.mqlidea2.clangd;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.settings.Mql5Settings;

/**
 * Tools 菜单中的 "Setup clangd for MQL5" Action
 * 一键生成 .clangd 配置文件
 */
public class ClangdSetupAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        Mql5Settings settings = Mql5Settings.getInstance();

        if (settings.getMql5DataDir() == null || settings.getMql5DataDir().isEmpty()) {
            notify(project, "MQL5 DevKit",
                "MQL5 data directory not configured. Please set it in Settings > Tools > MQL5 DevKit.",
                NotificationType.WARNING);
            return;
        }

        String basePath = project.getBasePath();
        if (basePath == null) return;

        boolean success = ClangdConfigGenerator.writeToFile(basePath, settings);
        if (success) {
            notify(project, "MQL5 DevKit",
                ".clangd configuration generated successfully at project root.\n" +
                "Make sure you have the clangd extension installed in IDEA.",
                NotificationType.INFORMATION);
        } else {
            notify(project, "MQL5 DevKit",
                "Failed to generate .clangd configuration.",
                NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    private void notify(Project project, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("MQL5DevKit")
            .createNotification(title, content, type)
            .notify(project);
    }
}
