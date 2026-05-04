package ru.investflow.mqlidea2.editor;

import com.intellij.codeInsight.daemon.LineMarkerInfo;
import com.intellij.codeInsight.daemon.LineMarkerProvider;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.editor.markup.GutterIconRenderer;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiManager;
import com.intellij.psi.search.FilenameIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.psi.MQL4Elements;

import java.util.List;

/**
 * Line marker provider for MQL4/MQL5 files.
 * Shows icons for entry points and #include navigation.
 */
public class MQL4LineMarkerProvider implements LineMarkerProvider {

    private static final List<String> ENTRY_POINTS = List.of(
            "OnTick", "OnCalculate", "OnStart", "OnChartEvent",
            "OnInit", "OnDeinit", "OnTimer", "OnTrade", "OnTradeTransaction",
            "OnTester", "OnBookEvent", "OnTickVolume");

    @Override
    public @Nullable LineMarkerInfo<?> getLineMarkerInfo(@NotNull PsiElement element) {
        if (element.getNode().getElementType() == MQL4Elements.IDENTIFIER) {
            String text = element.getText();
            if (ENTRY_POINTS.contains(text)) {
                PsiElement parent = element.getParent();
                if (parent != null && parent.getNode() != null &&
                        (parent.getNode().getElementType() == MQL4Elements.FUNCTION ||
                                parent.getNode().getElementType() == MQL4Elements.FUNCTION_DECLARATION)) {
                    return new LineMarkerInfo<>(
                            element, element.getTextRange(),
                            AllIcons.Actions.Execute,
                            el -> "MQL5 entry point: " + text,
                            null, GutterIconRenderer.Alignment.LEFT);
                }
            }
        }

        if (element.getNode().getElementType() == MQL4Elements.INCLUDE_STRING_LITERAL) {
            String includePath = element.getText();
            if (includePath.length() > 2) {
                includePath = includePath.substring(1, includePath.length() - 1);
            }
            String finalPath = includePath;
            return new LineMarkerInfo<>(
                    element, element.getTextRange(),
                    AllIcons.Actions.EditSource,
                    el -> "Go to " + finalPath,
                    (e, elt) -> navigateToInclude(elt.getProject(), finalPath),
                    GutterIconRenderer.Alignment.LEFT);
        }

        return null;
    }

    private static void navigateToInclude(Project project, String includePath) {
        String fileName = includePath;
        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash >= 0) {
            fileName = fileName.substring(lastSlash + 1);
        }
        PsiElement[] files = FilenameIndex.getFilesByName(
                project, fileName, GlobalSearchScope.projectScope(project));
        if (files.length > 0 && files[0] instanceof com.intellij.psi.PsiFile psiFile) {
            com.intellij.openapi.fileEditor.FileEditorManager.getInstance(project)
                    .openFile(psiFile.getVirtualFile(), true);
        }
    }
}
