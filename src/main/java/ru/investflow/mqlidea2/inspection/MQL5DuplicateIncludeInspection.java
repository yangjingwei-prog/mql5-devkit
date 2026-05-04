package ru.investflow.mqlidea2.inspection;

import com.intellij.codeInspection.InspectionManager;
import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4FileType;
import ru.investflow.mqlidea2.psi.MQL4Elements;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.ArrayList;

/**
 * Inspection: warns about duplicate #include directives in the same file.
 * Provides quick fix to remove the duplicate.
 */
public class MQL5DuplicateIncludeInspection extends LocalInspectionTool {

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (file.getFileType() != MQL4FileType.INSTANCE) {
            return null;
        }

        Set<String> seen = new HashSet<>();
        List<ProblemDescriptor> descriptors = new ArrayList<>();

        for (PsiElement child : file.getChildren()) {
            if (child.getNode().getElementType() != MQL4Elements.PREPROCESSOR_INCLUDE_BLOCK) {
                continue;
            }
            // Get the include path string
            var stringNode = child.getNode().findChildByType(MQL4Elements.INCLUDE_STRING_LITERAL);
            if (stringNode == null) continue;
            String path = stringNode.getText();
            if (seen.contains(path)) {
                descriptors.add(manager.createProblemDescriptor(child, child,
                        "Duplicate #include directive",
                        ProblemHighlightType.WARNING, isOnTheFly,
                        new RemoveDuplicateIncludeFix()));
            } else {
                seen.add(path);
            }
        }
        return descriptors.isEmpty() ? null : descriptors.toArray(new ProblemDescriptor[0]);
    }

    private static class RemoveDuplicateIncludeFix implements LocalQuickFix {
        @NotNull
        @Override
        public String getFamilyName() {
            return "Remove duplicate #include";
        }

        @Override
        public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
            PsiElement element = descriptor.getPsiElement();
            WriteCommandAction.runWriteCommandAction(project, () -> {
                // Delete the element and any trailing whitespace
                PsiElement next = element.getNextSibling();
                element.delete();
                if (next != null && next.getNode().getElementType() == MQL4Elements.WHITE_SPACE) {
                    next.delete();
                }
            });
        }
    }
}
