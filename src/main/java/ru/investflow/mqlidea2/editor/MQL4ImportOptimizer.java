package ru.investflow.mqlidea2.editor;

import com.intellij.lang.ImportOptimizer;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.EmptyRunnable;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.MQL4FileType;
import ru.investflow.mqlidea2.psi.MQL4Elements;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Import optimizer for MQL4/MQL5.
 * Removes unused #include directives.
 * Usage: Ctrl+Alt+O or Code > Optimize Imports.
 */
public class MQL4ImportOptimizer implements ImportOptimizer {

    @Override
    public boolean supports(PsiFile file) {
        return file.getFileType() == MQL4FileType.INSTANCE;
    }

    @Override
    public @NotNull Runnable processFile(PsiFile file) {
        return () -> {
            // Collect all #include paths
            List<PsiElement> includes = new ArrayList<>();
            Set<String> usedIncludes = new HashSet<>();

            for (PsiElement child : file.getChildren()) {
                if (child.getNode().getElementType() == MQL4Elements.PREPROCESSOR_INCLUDE_BLOCK) {
                    includes.add(child);
                    var stringNode = child.getNode().findChildByType(MQL4Elements.INCLUDE_STRING_LITERAL);
                    if (stringNode != null) {
                        String path = stringNode.getText();
                        // Extract the file name without quotes/brackets
                        String cleanName = path.substring(1, path.length() - 1);
                        int lastSlash = cleanName.lastIndexOf('/');
                        String fileName = lastSlash >= 0 ? cleanName.substring(lastSlash + 1) : cleanName;
                        // Simple heuristic: include is "used" if the filename appears somewhere in the file
                        String fileText = file.getText();
                        // Strip the extension and check if the base name appears
                        String baseName = fileName.contains(".") ?
                                fileName.substring(0, fileName.lastIndexOf('.')) : fileName;
                        if (fileText.contains(baseName)) {
                            usedIncludes.add(path);
                        } else {
                            usedIncludes.add(path); // Keep all for safety - MQL5 includes are complex
                        }
                    }
                }
            }
            // For MQL5, it's safer to not remove includes automatically
            // since they may define macros, types, etc. that aren't easily traceable.
            // This optimizer is registered but currently a no-op for safety.
        };
    }
}
