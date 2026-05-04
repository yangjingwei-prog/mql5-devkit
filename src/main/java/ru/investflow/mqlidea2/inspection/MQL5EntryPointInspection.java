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

import java.util.List;
import java.util.Set;

/**
 * Inspection: warns if .mq5/.mq4 file has no standard entry point function.
 * Entry points: OnTick (EA), OnCalculate (indicator), OnStart (script),
 *               OnService (service), OnInit, OnDeinit.
 */
public class MQL5EntryPointInspection extends LocalInspectionTool {

    private static final Set<String> ENTRY_POINTS = Set.of(
            "OnTick", "OnCalculate", "OnStart", "OnChartEvent",
            "OnInit", "OnDeinit", "OnTimer", "OnService");

    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (file.getFileType() != MQL4FileType.INSTANCE) {
            return null;
        }
        String name = file.getName().toLowerCase();
        if (name.endsWith(".mqh")) {
            return null; // Headers don't need entry points
        }

        boolean hasEntryPoint = false;
        for (PsiElement child : file.getChildren()) {
            if (child.getNode().getElementType() == MQL4Elements.FUNCTION) {
                // Find the function name
                var nameNode = child.getNode().findChildByType(MQL4Elements.IDENTIFIER);
                if (nameNode != null && ENTRY_POINTS.contains(nameNode.getText())) {
                    hasEntryPoint = true;
                    break;
                }
            }
        }

        if (!hasEntryPoint) {
            PsiElement firstChild = file.getFirstChild();
            if (firstChild == null) firstChild = file;
            return new ProblemDescriptor[]{
                    manager.createProblemDescriptor(file, firstChild,
                            "No MQL5 entry point function found (e.g., OnTick, OnCalculate, OnStart)",
                            ProblemHighlightType.WEAK_WARNING, isOnTheFly)
            };
        }
        return null;
    }
}
