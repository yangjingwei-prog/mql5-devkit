package ru.investflow.mqlidea2.editor;

import com.intellij.codeInsight.editorActions.TypedHandlerDelegate;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.MQL4FileType;

/**
 * Auto-pair handler for single and double quotes in MQL4/MQL5 files.
 */
public class MQL4TypedHandler extends TypedHandlerDelegate {

    @Override
    public @NotNull Result charTyped(char c, @NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
        if (file.getFileType() != MQL4FileType.INSTANCE) {
            return Result.CONTINUE;
        }

        if (c == '"' || c == '\'') {
            int offset = editor.getCaretModel().getOffset();
            CharSequence text = editor.getDocument().getCharsSequence();
            // If the next char is the same quote, skip it instead of inserting a new pair
            if (offset < text.length() && text.charAt(offset) == c) {
                editor.getDocument().deleteString(offset, offset + 1);
                return Result.STOP;
            }
            // Insert closing quote
            editor.getDocument().insertString(offset, String.valueOf(c));
            return Result.STOP;
        }

        return Result.CONTINUE;
    }
}
