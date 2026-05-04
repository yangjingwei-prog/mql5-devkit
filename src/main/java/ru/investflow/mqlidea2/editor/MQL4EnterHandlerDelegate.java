package ru.investflow.mqlidea2.editor;

import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegate;
import com.intellij.codeInsight.editorActions.enter.EnterHandlerDelegateAdapter;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorActionHandler;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4FileType;

/**
 * Enter handler for MQL4/MQL5: auto-indents after pressing Enter inside a { } block.
 */
public class MQL4EnterHandlerDelegate extends EnterHandlerDelegateAdapter {

    @Override
    public Result preprocessEnter(@NotNull PsiFile file, @NotNull Editor editor,
                                  @NotNull Ref<Integer> caretOffsetRef, @NotNull Ref<Integer> caretAdvanceRef,
                                  @NotNull DataContext dataContext, @Nullable EditorActionHandler originalHandler) {
        if (file.getFileType() != MQL4FileType.INSTANCE) {
            return Result.Continue;
        }

        Document document = editor.getDocument();
        int offset = caretOffsetRef.get();
        CharSequence text = document.getCharsSequence();

        // Check if we're between { and }
        if (offset > 0 && offset < text.length()) {
            char before = text.charAt(offset - 1);
            char after = text.charAt(offset);
            if (before == '{' && after == '}') {
                // Insert newline, let the original handler handle Enter,
                // then reformat the block
                return Result.Default;
            }
        }

        return Result.Continue;
    }

    @Override
    public Result postProcessEnter(@NotNull PsiFile file, @NotNull Editor editor,
                                   @NotNull DataContext dataContext) {
        if (file.getFileType() != MQL4FileType.INSTANCE) {
            return Result.Continue;
        }

        int offset = editor.getCaretModel().getOffset();
        Document document = editor.getDocument();
        CharSequence text = document.getCharsSequence();

        // If after Enter we're between { }, format the area
        if (offset >= 2 && offset < text.length()) {
            String before = text.subSequence(offset - 2, offset).toString();
            if (before.endsWith("{\n") || before.endsWith("{\r\n")) {
                // Reformat the current line to get proper indentation
                int line = document.getLineNumber(offset);
                int lineStart = document.getLineStartOffset(line);
                int lineEnd = document.getLineEndOffset(line);
                PsiDocumentManager.getInstance(file.getProject()).commitDocument(document);
                CodeStyleManager.getInstance(file.getProject())
                        .adjustLineIndent(file, lineStart);
            }
        }

        return Result.Continue;
    }
}
