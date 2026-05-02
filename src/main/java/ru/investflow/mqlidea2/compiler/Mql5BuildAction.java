package ru.investflow.mqlidea2.compiler;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.settings.Mql5Settings;
import ru.investflow.mqlidea2.ui.Mql5BuildLogService;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * 一键编译当前 MQL5 文件
 * 快捷键: Ctrl+Shift+F9
 */
public class Mql5BuildAction extends AnAction {

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null) return;

        Mql5Settings settings = Mql5Settings.getInstance();
        String metaEditorPath = settings.getMetaEditorPath();

        if (metaEditorPath == null || metaEditorPath.isEmpty() || !new File(metaEditorPath).exists()) {
            notify(project, "MQL5 DevKit", "MetaEditor path not configured. Please set it in Settings > Tools > MQL5 DevKit.",
                NotificationType.ERROR);
            return;
        }

        String filePath = file.getCanonicalPath();
        if (filePath == null) return;

        // 如果是 .mqh 头文件，尝试找到引用它的 .mq5
        if ("mqh".equalsIgnoreCase(file.getExtension())) {
            notify(project, "MQL5 DevKit", "Smart compile target for .mqh files is not yet implemented.",
                NotificationType.WARNING);
            return;
        }

        // 执行编译
        compileFile(project, metaEditorPath, filePath, file.getName(), file);
    }

    private void compileFile(Project project, String metaEditorPath, String filePath, String fileName, VirtualFile virtualFile) {
        Mql5BuildLogService logService = Mql5BuildLogService.getInstance(project);
        logService.startBuild(fileName);

        try {
            ProcessBuilder pb = new ProcessBuilder(
                metaEditorPath,
                "/compile:" + filePath,
                "/log"
            );
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            logService.appendLine("Process exited with code: " + exitCode);

            // 读取 .log 文件
            String logPath = filePath.replaceAll("\\.(mq[45]|mql[45])$", ".log");
            File logFile = new File(logPath);
            if (logFile.exists()) {
                Path logPathFile = logFile.toPath();
                String logContent = new String(Files.readAllBytes(logPathFile));
                List<Mql5ErrorParser.Mql5Error> errors = Mql5ErrorParser.parseLog(logContent);

                // Write raw log content to Build Log
                if (!logContent.isBlank()) {
                    logService.appendLine("");
                    logService.appendLine("--- Compiler Output ---");
                    logService.append(logContent.endsWith("\n") ? logContent : logContent + "\n");
                }

                // Write parsed errors/warnings
                String summary = Mql5ErrorParser.getSummary(errors);
                logService.appendSummary(summary);

                // Write errors to cache for ExternalAnnotator
                Mql5CompilationResultCache cache = Mql5CompilationResultCache.getInstance(project);
                cache.put(filePath, errors);

                // Trigger re-annotation in editor
                triggerReAnnotation(project, virtualFile);

                NotificationType type = errors.stream().anyMatch(Mql5ErrorParser.Mql5Error::isError)
                    ? NotificationType.ERROR : NotificationType.INFORMATION;
                notify(project, "MQL5 Compilation", fileName + ": " + summary, type);

                logFile.delete();
            } else {
                logService.appendLine("Compilation completed (no log file generated).");
                notify(project, "MQL5 Compilation", "Compilation completed (no log file generated).",
                    NotificationType.INFORMATION);
            }
        } catch (Exception ex) {
            logService.appendLine("ERROR: " + ex.getMessage());
            notify(project, "MQL5 DevKit", "Compilation failed: " + ex.getMessage(),
                NotificationType.ERROR);
        }
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(
            file != null && isMql5File(file)
        );
    }

    private boolean isMql5File(VirtualFile file) {
        String ext = file.getExtension();
        return "mq5".equalsIgnoreCase(ext) || "mq4".equalsIgnoreCase(ext);
    }

    private void notify(Project project, String title, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
            .getNotificationGroup("MQL5DevKit")
            .createNotification(title, content, type)
            .notify(project);
    }

    private void triggerReAnnotation(Project project, VirtualFile file) {
        com.intellij.openapi.application.ApplicationManager.getApplication().invokeLater(() -> {
            if (project.isDisposed()) return;
            Document document = FileDocumentManager.getInstance().getDocument(file);
            if (document != null) {
                PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document);
                if (psiFile != null) {
                    DaemonCodeAnalyzer.getInstance(project).restart(psiFile);
                }
            }
        });
    }
}
