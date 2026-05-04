package ru.investflow.mqlidea2.compiler;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4FileType;

import java.util.Collection;

/**
 * Smart compile target: when editing a .mqh header file,
 * find the .mq5/.mq4 file that includes it and compile that instead.
 */
public class Mql5SmartCompileTarget {

    /**
     * Find the best compile target for the given file.
     * For .mqh files, scans all .mq5/.mq4 files in the project for #include directives
     * that reference this .mqh file.
     *
     * @return the VirtualFile to compile, or null if none found
     */
    @Nullable
    public static VirtualFile findCompileTarget(@NotNull VirtualFile headerFile, @NotNull Project project) {
        String headerName = headerFile.getName();
        String headerPath = headerFile.getCanonicalPath();
        if (headerPath == null) return null;

        Collection<VirtualFile> candidates = FileTypeIndex.getFiles(MQL4FileType.INSTANCE,
                GlobalSearchScope.projectScope(project));

        VirtualFile bestMatch = null;
        int bestScore = -1;

        for (VirtualFile candidate : candidates) {
            String ext = candidate.getExtension();
            // Only compile .mq5/.mq4 files (not .mqh themselves)
            if ("mqh".equalsIgnoreCase(ext)) continue;

            int score = getIncludeScore(candidate, headerFile, project);
            if (score > bestScore) {
                bestScore = score;
                bestMatch = candidate;
            }
        }

        return bestMatch;
    }

    /**
     * Score a candidate file based on how strongly it includes the header.
     * Higher score = better match.
     */
    private static int getIncludeScore(@NotNull VirtualFile candidate, @NotNull VirtualFile header, @NotNull Project project) {
        PsiFile psiFile = PsiManager.getInstance(project).findFile(candidate);
        if (psiFile == null) return 0;

        String text = psiFile.getText();
        String headerName = header.getName();
        int score = 0;

        // Check for exact relative path includes
        // e.g. #include " subdir/MyHeader.mqh"
        String parentName = header.getParent() != null ? header.getParent().getName() : null;
        if (parentName != null) {
            if (text.contains(parentName + "/" + headerName) || text.contains(parentName + "\\" + headerName)) {
                score += 10;
            }
        }

        // Check for simple name include: #include "HeaderName.mqh"
        // Use regex-free simple search for the include pattern
        int idx = 0;
        while ((idx = text.indexOf(headerName, idx)) != -1) {
            // Check context: should be inside an #include directive
            int lineStart = text.lastIndexOf('\n', idx);
            String line = text.substring(lineStart + 1, Math.min(idx + headerName.length() + 20, text.length())).trim();
            if (line.startsWith("#include")) {
                score += 5;
            }
            idx++;
        }

        return score;
    }
}
