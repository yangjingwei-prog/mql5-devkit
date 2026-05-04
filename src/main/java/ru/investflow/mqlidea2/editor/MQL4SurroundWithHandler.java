package ru.investflow.mqlidea2.editor;

import com.intellij.lang.surroundWith.SurroundDescriptor;
import com.intellij.lang.surroundWith.Surrounder;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Surround With handler for MQL4/MQL5 (Ctrl+Alt+T).
 */
public class MQL4SurroundWithHandler implements SurroundDescriptor {

    private static final MQL4Surrounder[] SURROUNDERS = {
            new MQL4IfSurrounder(),
            new MQL4IfElseSurrounder(),
            new MQL4ForSurrounder(),
            new MQL4WhileSurrounder(),
            new MQL4BracesSurrounder()
    };

    @Override
    public @NotNull Surrounder[] getSurrounders() {
        return SURROUNDERS;
    }

    @Override
    public boolean isExclusive() {
        return false;
    }

    @Override
    public @NotNull PsiElement[] getElementsToSurround(PsiFile file, int startOffset, int endOffset) {
        PsiElement element = file.findElementAt(startOffset);
        if (element != null) {
            return new PsiElement[]{element};
        }
        return PsiElement.EMPTY_ARRAY;
    }

    private abstract static class MQL4Surrounder implements Surrounder {
        @Override
        public boolean isApplicable(@NotNull PsiElement[] elements) {
            return elements.length > 0;
        }
    }

    private static class MQL4IfSurrounder extends MQL4Surrounder {
        @Override public @NotNull String getTemplateDescription() { return "if"; }

        @Override
        public @Nullable TextRange surroundElements(@NotNull Project project,
                                                     @NotNull Editor editor,
                                                     @NotNull PsiElement[] elements) {
            StringBuilder sb = new StringBuilder("if() {\n");
            for (PsiElement e : elements) sb.append(e.getText()).append("\n");
            sb.append("}");
            String text = sb.toString();
            int start = elements[0].getTextOffset();
            int end = elements[elements.length - 1].getTextRange().getEndOffset();
            editor.getDocument().replaceString(start, end, text);
            editor.getCaretModel().moveToOffset(start + 3);
            return new TextRange(start, start + text.length());
        }
    }

    private static class MQL4IfElseSurrounder extends MQL4Surrounder {
        @Override public @NotNull String getTemplateDescription() { return "if / else"; }

        @Override
        public @Nullable TextRange surroundElements(@NotNull Project project,
                                                     @NotNull Editor editor,
                                                     @NotNull PsiElement[] elements) {
            StringBuilder sb = new StringBuilder("if() {\n");
            for (PsiElement e : elements) sb.append(e.getText()).append("\n");
            sb.append("} else {\n}");
            String text = sb.toString();
            int start = elements[0].getTextOffset();
            int end = elements[elements.length - 1].getTextRange().getEndOffset();
            editor.getDocument().replaceString(start, end, text);
            editor.getCaretModel().moveToOffset(start + 3);
            return new TextRange(start, start + text.length());
        }
    }

    private static class MQL4ForSurrounder extends MQL4Surrounder {
        @Override public @NotNull String getTemplateDescription() { return "for"; }

        @Override
        public @Nullable TextRange surroundElements(@NotNull Project project,
                                                     @NotNull Editor editor,
                                                     @NotNull PsiElement[] elements) {
            StringBuilder sb = new StringBuilder("for(int i = 0; i < ; i++) {\n");
            for (PsiElement e : elements) sb.append(e.getText()).append("\n");
            sb.append("}");
            String text = sb.toString();
            int start = elements[0].getTextOffset();
            int end = elements[elements.length - 1].getTextRange().getEndOffset();
            editor.getDocument().replaceString(start, end, text);
            editor.getCaretModel().moveToOffset(start + 26);
            return new TextRange(start, start + text.length());
        }
    }

    private static class MQL4WhileSurrounder extends MQL4Surrounder {
        @Override public @NotNull String getTemplateDescription() { return "while"; }

        @Override
        public @Nullable TextRange surroundElements(@NotNull Project project,
                                                     @NotNull Editor editor,
                                                     @NotNull PsiElement[] elements) {
            StringBuilder sb = new StringBuilder("while() {\n");
            for (PsiElement e : elements) sb.append(e.getText()).append("\n");
            sb.append("}");
            String text = sb.toString();
            int start = elements[0].getTextOffset();
            int end = elements[elements.length - 1].getTextRange().getEndOffset();
            editor.getDocument().replaceString(start, end, text);
            editor.getCaretModel().moveToOffset(start + 6);
            return new TextRange(start, start + text.length());
        }
    }

    private static class MQL4BracesSurrounder extends MQL4Surrounder {
        @Override public @NotNull String getTemplateDescription() { return "{ }"; }

        @Override
        public @Nullable TextRange surroundElements(@NotNull Project project,
                                                     @NotNull Editor editor,
                                                     @NotNull PsiElement[] elements) {
            StringBuilder sb = new StringBuilder("{\n");
            for (PsiElement e : elements) sb.append(e.getText()).append("\n");
            sb.append("}");
            String text = sb.toString();
            int start = elements[0].getTextOffset();
            int end = elements[elements.length - 1].getTextRange().getEndOffset();
            editor.getDocument().replaceString(start, end, text);
            return new TextRange(start, start + text.length());
        }
    }
}
