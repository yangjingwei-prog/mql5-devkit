package ru.investflow.mqlidea2.action;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.MQL4FileType;
import ru.investflow.mqlidea2.MQL4Icons;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;

/**
 * Creates a new MQL5 file from a bundled template.
 * Each static inner class corresponds to one file type in the New → MQL5 menu.
 */
public abstract class CreateMQL5FileAction extends AnAction {

    private final String templateName;
    private final String extension;

    protected CreateMQL5FileAction(String text, String description,
                                   String templateName, String extension) {
        super(text, description, MQL4Icons.File);
        this.templateName = templateName;
        this.extension = extension;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        // Ask for file name
        String name = Messages.showInputDialog(project,
                "Enter " + templateName + " file name (without extension):",
                "New " + templateName,
                MQL4Icons.File,
                "",
                null);
        if (name == null || name.trim().isEmpty()) return;
        name = name.trim();

        // Remove extension if user typed it
        if (name.endsWith("." + extension)) {
            name = name.substring(0, name.length() - extension.length() - 1);
        }

        // Find target directory from event
        PsiDirectory dir = getTargetDirectory(e);
        if (dir == null) return;

        String fileName = name + "." + extension;

        // Check if file already exists
        if (dir.findFile(fileName) != null) {
            Messages.showErrorDialog(project, "File '" + fileName + "' already exists.", "Error");
            return;
        }

        String content = buildContent(project, name);
        String finalName = name;
        WriteCommandAction.runWriteCommandAction(project, "Create " + fileName, null, () -> {
            PsiFileFactory factory = PsiFileFactory.getInstance(project);
            PsiFile file = factory.createFileFromText(fileName, MQL4FileType.INSTANCE, content);
            dir.add(file);
        });
    }

    private String buildContent(Project project, String name) {
        // Load template directly from classpath: /fileTemplates/internal/<Name>.<ext>.ft
        String resourcePath = "/fileTemplates/internal/" + templateName + "." + extension + ".ft";
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is != null) {
                String template = new String(is.readAllBytes(), StandardCharsets.UTF_8);
                template = template.replace("${NAME}", name);
                template = template.replace("${YEAR}", String.valueOf(Year.now().getValue()));
                template = template.replace("${USER}", System.getProperty("user.name", ""));
                return template;
            }
        } catch (IOException ignored) {
        }
        // Fallback: minimal file
        return "// " + name + "." + extension + "\n";
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabledAndVisible(e.getProject() != null);
    }

    private static PsiDirectory getTargetDirectory(AnActionEvent e) {
        // Try to get directory from DataKeys
        com.intellij.openapi.vfs.VirtualFile vFile =
                e.getData(com.intellij.openapi.actionSystem.CommonDataKeys.VIRTUAL_FILE);
        Project project = e.getProject();
        if (project == null || vFile == null) return null;

        com.intellij.psi.PsiManager psiManager = com.intellij.psi.PsiManager.getInstance(project);
        if (vFile.isDirectory()) {
            return psiManager.findDirectory(vFile);
        } else {
            com.intellij.openapi.vfs.VirtualFile parent = vFile.getParent();
            return parent != null ? psiManager.findDirectory(parent) : null;
        }
    }

    // ---- Static inner classes for each file type ----

    public static class Ea extends CreateMQL5FileAction {
        public Ea() { super("MQL5 Expert Advisor", "Create a new MQL5 Expert Advisor", "MQL5 Expert Advisor", "mq5"); }
    }

    public static class Indicator extends CreateMQL5FileAction {
        public Indicator() { super("MQL5 Indicator", "Create a new MQL5 Indicator", "MQL5 Indicator", "mq5"); }
    }

    public static class Script extends CreateMQL5FileAction {
        public Script() { super("MQL5 Script", "Create a new MQL5 Script", "MQL5 Script", "mq5"); }
    }

    public static class Service extends CreateMQL5FileAction {
        public Service() { super("MQL5 Service", "Create a new MQL5 Service", "MQL5 Service", "mq5"); }
    }

    public static class Include extends CreateMQL5FileAction {
        public Include() { super("MQL5 Include File", "Create a new MQL5 include file (.mqh)", "MQL5 Include", "mqh"); }
    }

    public static class Mq4 extends CreateMQL5FileAction {
        public Mq4() { super("MQL4 File", "Create a new MQL4 file", "MQL4 File", "mq4"); }
    }
}
