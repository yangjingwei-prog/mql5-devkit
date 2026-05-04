package ru.investflow.mqlidea2.action;

import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.MQL4Icons;
import ru.investflow.mqlidea2.settings.Mql5Settings;

import java.io.File;

/**
 * 在 MetaEditor 中打开当前文件。
 */
public class OpenInMetaEditorAction extends AnAction {

    public OpenInMetaEditorAction() {
        super("Open in MetaEditor", "Open current file in MetaEditor 5", MQL4Icons.MetaEditor);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null) return;

        Mql5Settings settings = Mql5Settings.getInstance();
        String metaEditorPath = settings.getMetaEditorPath();

        if (metaEditorPath == null || metaEditorPath.isEmpty() || !new File(metaEditorPath).exists()) {
            notify(project, "MetaEditor path not configured.\nSettings > Tools > MQL5 DevKit.", NotificationType.ERROR);
            return;
        }

        String filePath = file.getCanonicalPath();
        if (filePath == null) return;

        try {
            ProcessBuilder pb = new ProcessBuilder(metaEditorPath, filePath);
            pb.redirectErrorStream(true);
            pb.start();
        } catch (Exception ex) {
            notify(project, "Failed to open MetaEditor: " + ex.getMessage(), NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(file != null && isMql5File(file));
    }

    private boolean isMql5File(VirtualFile file) {
        String ext = file.getExtension();
        return "mq5".equalsIgnoreCase(ext) || "mq4".equalsIgnoreCase(ext)
                || "mqh".equalsIgnoreCase(ext);
    }

    private void notify(Project project, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("MQL5DevKit")
                .createNotification("MQL5 DevKit", content, type)
                .notify(project);
    }
}
