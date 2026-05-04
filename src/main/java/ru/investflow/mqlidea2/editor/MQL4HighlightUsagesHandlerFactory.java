package ru.investflow.mqlidea2.editor;

import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerBase;
import com.intellij.codeInsight.highlighting.HighlightUsagesHandlerFactoryBase;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.util.Consumer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4FileType;
import ru.investflow.mqlidea2.psi.MQL4Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Highlight usages handler for MQL4/MQL5 (Ctrl+Shift+F7).
 */
public class MQL4HighlightUsagesHandlerFactory extends HighlightUsagesHandlerFactoryBase {

    @Override
    public @Nullable HighlightUsagesHandlerBase<PsiElement> createHighlightUsagesHandler(
            @NotNull Editor editor, @NotNull PsiFile file, @NotNull PsiElement target) {
        if (file.getFileType() != MQL4FileType.INSTANCE) {
            return null;
        }
        PsiElement element = file.findElementAt(editor.getCaretModel().getOffset());
        if (element == null) return null;

        if (element.getNode().getElementType() == MQL4Elements.IDENTIFIER) {
            return new MQL4HighlightHandler(editor, file, element);
        }
        return null;
    }

    private static class MQL4HighlightHandler extends HighlightUsagesHandlerBase<PsiElement> {
        private final PsiElement target;

        MQL4HighlightHandler(Editor editor, PsiFile file, PsiElement target) {
            super(editor, file);
            this.target = target;
        }

        @Override
        public @NotNull List<PsiElement> getTargets() {
            return Collections.singletonList(target);
        }

        @Override
        public void computeUsages(@NotNull List<? extends PsiElement> targets) {
            String text = target.getText();
            myFile.accept(new PsiRecursiveElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element.getNode().getElementType() == MQL4Elements.IDENTIFIER &&
                            element.getText().equals(text)) {
                        addOccurrence(element);
                    }
                    super.visitElement(element);
                }
            });
        }

        @Override
        public void selectTargets(@NotNull List<? extends PsiElement> targets,
                                  @NotNull Consumer<? super List<? extends PsiElement>> selectionConsumer) {
            selectionConsumer.consume(targets);
        }
    }
}
