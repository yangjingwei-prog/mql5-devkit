package ru.investflow.mqlidea2.compiler;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileDocumentManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.project.ProjectManager;
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
 * Compile on Save handler — listens for document save events
 * and triggers MetaEditor compilation for MQL5 files.
 */
public class Mql5CompileOnSaveHandler implements FileDocumentManagerListener {

    @Override
    public void beforeDocumentSaving(@NotNull Document document) {
        Mql5Settings settings = Mql5Settings.getInstance();
        if (!settings.isCompileOnSave()) return;

        VirtualFile file = FileDocumentManager.getInstance().getFile(document);
        if (file == null || !isMqlFile(file)) return;

        String metaEditorPath = settings.getMetaEditorPath();
        if (metaEditorPath == null || metaEditorPath.isEmpty() || !new File(metaEditorPath).exists()) {
            return;
        }

        String filePath = file.getCanonicalPath();
        if (filePath == null) return;

        // Find an open project for this file
        Project project = findProjectForFile(file);
        if (project == null) return;

        // For .mqh files, find the compile target
        if ("mqh".equalsIgnoreCase(file.getExtension())) {
            VirtualFile target = Mql5SmartCompileTarget.findCompileTarget(file, project);
            if (target != null && target.getCanonicalPath() != null) {
                compileFile(project, metaEditorPath, target.getCanonicalPath(),
                        target.getName() + " (via " + file.getName() + ")", target);
            }
            return;
        }

        compileFile(project, metaEditorPath, filePath, file.getName(), file);
    }

    private void compileFile(Project project, String metaEditorPath, String filePath, String fileName, VirtualFile virtualFile) {
        Mql5BuildLogService logService = Mql5BuildLogService.getInstance(project);
        logService.startBuild(fileName + " (auto-compile on save)");

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

            // Read .log file
            String logPathStr = filePath.replaceAll("\\.(mq[45]|mql[45])$", ".log");
            File logFile = new File(logPathStr);
            if (logFile.exists()) {
                Path logPathFile = logFile.toPath();
                String logContent = new String(Files.readAllBytes(logPathFile));
                List<Mql5ErrorParser.Mql5Error> errors = Mql5ErrorParser.parseLog(logContent);

                if (!logContent.isBlank()) {
                    logService.appendLine("");
                    logService.appendLine("--- Compiler Output ---");
                    logService.append(logContent.endsWith("\n") ? logContent : logContent + "\n");
                }

                String summary = Mql5ErrorParser.getSummary(errors);
                logService.appendSummary(summary);

                // Write errors to cache for ExternalAnnotator
                Mql5CompilationResultCache cache = Mql5CompilationResultCache.getInstance(project);
                cache.put(filePath, errors);

                // Trigger re-annotation in editor
                triggerReAnnotation(project, virtualFile);

                if (Mql5Settings.getInstance().isShowNotification()) {
                    NotificationType type = errors.stream().anyMatch(Mql5ErrorParser.Mql5Error::isError)
                        ? NotificationType.ERROR : NotificationType.INFORMATION;
                    notify(project, "MQL5 Auto-Compile", fileName + ": " + summary, type);
                }

                logFile.delete();
            } else {
                logService.appendLine("Compilation completed (no log file generated).");
            }
        } catch (Exception ex) {
            logService.appendLine("ERROR: " + ex.getMessage());
        }
    }

    private Project findProjectForFile(@NotNull VirtualFile file) {
        for (Project project : ProjectManager.getInstance().getOpenProjects()) {
            if (!project.isDisposed()) {
                return project;
            }
        }
        return null;
    }

    private boolean isMqlFile(@NotNull VirtualFile file) {
        String ext = file.getExtension();
        return "mq5".equalsIgnoreCase(ext)
            || "mq4".equalsIgnoreCase(ext)
            || "mql5".equalsIgnoreCase(ext)
            || "mql4".equalsIgnoreCase(ext);
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
