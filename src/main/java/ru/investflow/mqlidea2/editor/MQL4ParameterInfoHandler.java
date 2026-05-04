package ru.investflow.mqlidea2.editor;

import com.intellij.lang.parameterInfo.CreateParameterInfoContext;
import com.intellij.lang.parameterInfo.ParameterInfoHandler;
import com.intellij.lang.parameterInfo.ParameterInfoUIContext;
import com.intellij.lang.parameterInfo.UpdateParameterInfoContext;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4Language;
import ru.investflow.mqlidea2.doc.DocEntry;
import ru.investflow.mqlidea2.doc.DocEntryType;
import ru.investflow.mqlidea2.doc.MQL4DocumentationProvider;
import ru.investflow.mqlidea2.index.MQL4FunctionNameIndex;
import ru.investflow.mqlidea2.psi.MQL4Elements;
import ru.investflow.mqlidea2.psi.impl.MQL4FunctionElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Parameter Info handler (Ctrl+P) for MQL4/MQL5.
 * Shows function parameter hints for user-defined and built-in functions.
 */
@SuppressWarnings("rawtypes")
public class MQL4ParameterInfoHandler implements ParameterInfoHandler {

    @Override
    public @Nullable Object findElementForParameterInfo(@NotNull CreateParameterInfoContext context) {
        return findArgumentList(context.getFile(), context.getOffset());
    }

    @Override
    public @Nullable Object findElementForUpdatingParameterInfo(@NotNull UpdateParameterInfoContext context) {
        return findArgumentList(context.getFile(), context.getOffset());
    }

    @Override
    public void showParameterInfo(@NotNull Object element, @NotNull CreateParameterInfoContext context) {
        if (!(element instanceof PsiElement)) return;
        PsiElement psi = (PsiElement) element;
        String funcName = getFunctionNameBeforeBrackets(psi);
        if (funcName == null) return;

        List<Object> items = resolveFunctionParams(funcName, psi);
        if (!items.isEmpty()) {
            context.setItemsToShow(items.toArray());
            context.showHint(psi, psi.getTextRange().getStartOffset(), this);
        }
    }

    @Override
    public void updateParameterInfo(@NotNull Object p, @NotNull UpdateParameterInfoContext context) {
        if (!(p instanceof PsiElement)) return;
        PsiElement list = (PsiElement) p;
        if (context.getParameterOwner() != list) {
            context.removeHint();
            return;
        }
        int offset = context.getOffset();
        int currentParam = 0;
        for (PsiElement child : list.getChildren()) {
            if (child.getTextRange().getStartOffset() >= offset) break;
            if (child.getNode().getElementType() == MQL4Elements.COMMA) {
                currentParam++;
            }
        }
        context.setCurrentParameter(currentParam);
    }

    @Override
    public void updateUI(@NotNull Object p, @NotNull ParameterInfoUIContext context) {
        if (p instanceof FunctionParamInfo) {
            FunctionParamInfo info = (FunctionParamInfo) p;
            context.setupUIComponentPresentation(
                    info.displayText,
                    info.getHighlightStart(context.getCurrentParameterIndex()),
                    info.getHighlightEnd(context.getCurrentParameterIndex()),
                    false,
                    false,
                    false,
                    context.getDefaultParameterColor()
            );
        }
    }

    @Nullable
    private PsiElement findArgumentList(PsiFile file, int offset) {
        PsiElement element = file.findElementAt(offset);
        if (element == null && offset > 0) {
            element = file.findElementAt(offset - 1);
        }
        if (element == null) return null;

        PsiElement brackets = element;
        while (brackets != null && brackets != file) {
            if (brackets.getNode().getElementType() == MQL4Elements.BRACKETS_BLOCK) {
                PsiElement prev = PsiTreeUtil.skipWhitespacesAndCommentsBackward(brackets);
                if (prev != null && prev.getNode().getElementType() == MQL4Elements.IDENTIFIER) {
                    return brackets;
                }
            }
            brackets = brackets.getParent();
        }
        return null;
    }

    @Nullable
    private String getFunctionNameBeforeBrackets(@NotNull PsiElement bracketsBlock) {
        PsiElement prev = PsiTreeUtil.skipWhitespacesAndCommentsBackward(bracketsBlock);
        if (prev != null && prev.getNode().getElementType() == MQL4Elements.IDENTIFIER) {
            return prev.getText();
        }
        return null;
    }

    @NotNull
    private List<Object> resolveFunctionParams(@NotNull String funcName, @NotNull PsiElement context) {
        List<Object> result = new ArrayList<>();

        // Check user-defined functions via index
        Collection<MQL4FunctionElement> functions =
                MQL4FunctionNameIndex.getInstance().get(funcName, context.getProject(),
                        com.intellij.psi.search.GlobalSearchScope.allScope(context.getProject()));
        for (MQL4FunctionElement fn : functions) {
            String sig = fn.getSignature();
            if (!sig.isEmpty()) {
                result.add(new FunctionParamInfo(fn.getFunctionName() + "(" + sig + ")", sig));
            }
        }

        // Check built-in functions
        DocEntry entry = MQL4DocumentationProvider.getEntryByText(funcName);
        if (entry != null && entry.type == DocEntryType.BuiltInFunction) {
            result.add(new FunctionParamInfo(funcName + "(...)", ""));
        }

        return result;
    }

    private static class FunctionParamInfo {
        final String displayText;
        final String paramList;

        FunctionParamInfo(String displayText, String paramList) {
            this.displayText = displayText;
            this.paramList = paramList;
        }

        int getHighlightStart(int paramIndex) {
            if (paramList.isEmpty() || paramIndex < 0) return -1;
            int start = 0;
            int depth = 0;
            int currentParam = 0;
            for (int i = 0; i < paramList.length(); i++) {
                char c = paramList.charAt(i);
                if (c == '(' || c == '[' || c == '<') depth++;
                else if (c == ')' || c == ']' || c == '>') depth--;
                else if (c == ',' && depth == 0) {
                    if (currentParam == paramIndex) return start;
                    currentParam++;
                    start = i + 1;
                }
            }
            return currentParam == paramIndex ? start : -1;
        }

        int getHighlightEnd(int paramIndex) {
            if (paramList.isEmpty() || paramIndex < 0) return -1;
            int depth = 0;
            int currentParam = 0;
            for (int i = 0; i < paramList.length(); i++) {
                char c = paramList.charAt(i);
                if (c == '(' || c == '[' || c == '<') depth++;
                else if (c == ')' || c == ']' || c == '>') depth--;
                else if (c == ',' && depth == 0) {
                    if (currentParam == paramIndex) return i;
                    currentParam++;
                }
            }
            return currentParam == paramIndex ? paramList.length() : -1;
        }
    }
}
