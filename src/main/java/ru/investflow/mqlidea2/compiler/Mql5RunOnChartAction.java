package ru.investflow.mqlidea2.compiler;

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
import ru.investflow.mqlidea2.ui.Mql5BuildLogService;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

/**
 * 编译当前 MQL5 文件并部署到 MetaTrader 5 终端。
 */
public class Mql5RunOnChartAction extends AnAction {

    enum Mql5FileType {
        EXPERT("Experts", "EA"),
        INDICATOR("Indicators", "Indicator"),
        SCRIPT("Scripts", "Script");

        final String folder;
        final String label;
        Mql5FileType(String folder, String label) {
            this.folder = folder;
            this.label = label;
        }
    }

    public Mql5RunOnChartAction() {
        super("Run on Chart", "Compile and run in MetaTrader 5 terminal", MQL4Icons.File);
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (project == null || file == null) return;

        Mql5Settings settings = Mql5Settings.getInstance();
        String metaEditorPath = settings.getMetaEditorPath();
        String terminalPath = settings.getTerminalPath();

        if (metaEditorPath == null || metaEditorPath.isEmpty() || !new File(metaEditorPath).exists()) {
            notify(project, "MetaEditor path not configured.\nSettings > Tools > MQL5 DevKit.", NotificationType.ERROR);
            return;
        }
        if (terminalPath == null || terminalPath.isEmpty() || !new File(terminalPath).exists()) {
            notify(project, "Terminal path not configured.\nSettings > Tools > MQL5 DevKit.", NotificationType.ERROR);
            return;
        }

        String filePath = file.getCanonicalPath();
        if (filePath == null) return;

        String mql5Dir = settings.getMql5DataDir();
        if (mql5Dir == null || mql5Dir.isEmpty()) {
            notify(project, "MQL5 Data Dir not configured.\nSettings > Tools > MQL5 DevKit.", NotificationType.ERROR);
            return;
        }

        Mql5BuildLogService logService = Mql5BuildLogService.getInstance(project);
        logService.startBuild(file.getName() + " (Run on Chart)");

        // Step 1: Compile
        CompileResult result = compile(metaEditorPath, filePath, file.getName(), logService);
        if (result.errors > 0) {
            notify(project, file.getName() + ": compilation failed (" + result.errors + " error(s)).", NotificationType.ERROR);
            return;
        }
        logService.appendLine("Compilation successful.\n");

        // Step 2: Detect file type
        Mql5FileType fileType = detectFileType(file);
        String name = file.getNameWithoutExtension();

        // Step 3: Deploy .ex5 to terminal's MQL5 directory
        boolean deployed = deployEx5(filePath, name, fileType, mql5Dir, project, logService);

        // Step 4: Launch terminal
        launchTerminal(terminalPath, project, name, fileType, deployed, logService);
    }

    // ---- Compilation ----

    private static class CompileResult {
        int errors = 0;
        int warnings = 0;
    }

    private CompileResult compile(String metaEditorPath, String filePath, String fileName,
                                  Mql5BuildLogService logService) {
        CompileResult result = new CompileResult();
        try {
            ProcessBuilder pb = new ProcessBuilder(metaEditorPath, "/compile:" + filePath, "/log");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            int exitCode = process.waitFor();
            logService.appendLine("Compile exited with code: " + exitCode);

            String logPath = filePath.replaceAll("\\.(mq[45]|mql[45])$", ".log");
            File logFile = new File(logPath);
            if (logFile.exists()) {
                String logContent = new String(Files.readAllBytes(logFile.toPath()));
                if (!logContent.isBlank()) {
                    logService.appendLine("--- Compiler Output ---\n" + logContent);
                }
                var errors = Mql5ErrorParser.parseLog(logContent);
                result.errors = (int) errors.stream().filter(Mql5ErrorParser.Mql5Error::isError).count();
                result.warnings = (int) errors.stream().filter(Mql5ErrorParser.Mql5Error::isWarning).count();
                logFile.delete();
            }
        } catch (Exception ex) {
            logService.appendLine("ERROR: " + ex.getMessage());
            result.errors = 1;
        }
        return result;
    }

    // ---- File Type Detection ----

    private Mql5FileType detectFileType(VirtualFile file) {
        try {
            String content = new String(file.contentsToByteArray(), StandardCharsets.UTF_8);
            if (content.contains("OnCalculate(")) return Mql5FileType.INDICATOR;
            if (content.contains("OnStart()")) return Mql5FileType.SCRIPT;
            return Mql5FileType.EXPERT;
        } catch (IOException ignored) {
            return Mql5FileType.EXPERT;
        }
    }

    // ---- Deploy .ex5 ----

    private boolean deployEx5(String sourcePath, String name, Mql5FileType fileType,
                              String mql5Dir, Project project, Mql5BuildLogService logService) {
        String ex5Path = sourcePath.replaceAll("\\.(mq[45]|mql[45])$", ".ex5");
        File ex5File = new File(ex5Path);
        if (!ex5File.exists()) {
            logService.appendLine("WARNING: .ex5 file not found at " + ex5Path);
            notify(project, ".ex5 not found. Compilation may have failed silently.", NotificationType.WARNING);
            return false;
        }

        Path targetDir = resolveMql5SubDir(mql5Dir, fileType.folder);
        Path targetFile = targetDir.resolve(name + ".ex5");

        try {
            Files.createDirectories(targetDir);
            Files.copy(ex5File.toPath(), targetFile, StandardCopyOption.REPLACE_EXISTING);
            logService.appendLine("Deployed: " + targetFile);
            return true;
        } catch (IOException ex) {
            logService.appendLine("Deploy failed: " + ex.getMessage());
            notify(project, "Deploy failed: " + ex.getMessage(), NotificationType.WARNING);
            return false;
        }
    }

    // ---- Launch Terminal ----

    private void launchTerminal(String terminalPath, Project project, String name,
                                Mql5FileType fileType, boolean deployed,
                                Mql5BuildLogService logService) {
        try {
            ProcessBuilder pb = new ProcessBuilder(terminalPath);
            pb.directory(new File(terminalPath).getParentFile());
            pb.redirectErrorStream(true);
            pb.start();

            String msg;
            if (deployed) {
                msg = fileType.label + " '" + name + "' deployed.\n" +
                        "In Navigator → " + fileType.folder + " → drag to chart.";
            } else {
                msg = "Terminal launched. Make sure '" + name + "' is in " + fileType.folder + " folder.";
            }

            logService.appendLine(msg.replace("\n", " "));
            notify(project, msg, NotificationType.INFORMATION);
        } catch (IOException ex) {
            notify(project, "Failed to launch terminal: " + ex.getMessage(), NotificationType.ERROR);
        }
    }

    // ---- Helpers ----

    private static Path resolveMql5SubDir(String mql5Dir, String subFolder) {
        if (mql5Dir.endsWith("MQL5") || mql5Dir.endsWith("MQL5\\") || mql5Dir.endsWith("MQL5/")) {
            return Path.of(mql5Dir, subFolder);
        }
        return Path.of(mql5Dir, "MQL5", subFolder);
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        VirtualFile file = e.getData(CommonDataKeys.VIRTUAL_FILE);
        e.getPresentation().setEnabledAndVisible(file != null && isMql5File(file));
    }

    private boolean isMql5File(VirtualFile file) {
        String ext = file.getExtension();
        return "mq5".equalsIgnoreCase(ext) || "mq4".equalsIgnoreCase(ext);
    }

    private void notify(Project project, String content, NotificationType type) {
        NotificationGroupManager.getInstance()
                .getNotificationGroup("MQL5DevKit")
                .createNotification("MQL5 Run on Chart", content, type)
                .notify(project);
    }
}
