package ru.investflow.mqlidea2.editor;

import com.intellij.formatting.*;
import com.intellij.lang.ASTNode;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.formatter.common.AbstractBlock;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.psi.MQL4Elements;
import ru.investflow.mqlidea2.psi.MQL4TokenSets;
import ru.investflow.mqlidea2.psi.stub.MQL4StubElements;

import java.util.ArrayList;
import java.util.List;

/**
 * Code formatter for MQL4/MQL5 files (Ctrl+Alt+L).
 */
public class MQL4CodeFormatter implements FormattingModelBuilder {

    @NotNull
    @Override
    public FormattingModel createModel(@NotNull FormattingContext context) {
        PsiElement element = context.getPsiElement();
        CodeStyleSettings settings = context.getCodeStyleSettings();
        MQL4Block rootBlock = new MQL4Block(element.getNode(), null, null, settings);
        return FormattingModelProvider.createFormattingModelForPsiFile(
                element.getContainingFile(), rootBlock, settings);
    }

    @Override
    public @NotNull TextRange getRangeAffectingIndent(PsiFile file, int offset, ASTNode elementAtOffset) {
        return file.getTextRange();
    }

    private static class MQL4Block extends AbstractBlock {
        private final CodeStyleSettings settings;

        protected MQL4Block(@NotNull ASTNode node, @Nullable Wrap wrap, @Nullable Alignment alignment, CodeStyleSettings settings) {
            super(node, wrap, alignment);
            this.settings = settings;
        }

        @Override
        protected List<Block> buildChildren() {
            List<Block> blocks = new ArrayList<>();
            ASTNode child = myNode.getFirstChildNode();
            while (child != null) {
                if (!MQL4TokenSets.COMMENTS_OR_WS.contains(child.getElementType())) {
                    blocks.add(new MQL4Block(child, null, null, settings));
                }
                child = child.getTreeNext();
            }
            return blocks;
        }

        @Override
        public @Nullable Spacing getSpacing(@Nullable Block child1, @NotNull Block child2) {
            return Spacing.createSpacing(0, Integer.MAX_VALUE, 0, false, 0);
        }

        @Override
        public boolean isLeaf() {
            return myNode.getFirstChildNode() == null;
        }

        @Override
        public @Nullable Indent getIndent() {
            ASTNode parent = myNode.getTreeParent();
            if (parent == null || parent.getElementType() == MQL4StubElements.FILE) {
                return Indent.getNoneIndent();
            }
            if (parent.getElementType() == MQL4Elements.CLASS_INNER_BLOCK ||
                    parent.getElementType() == MQL4Elements.BRACKETS_BLOCK) {
                if (myNode.getElementType() != MQL4Elements.L_CURLY_BRACKET &&
                        myNode.getElementType() != MQL4Elements.R_CURLY_BRACKET) {
                    return Indent.getNormalIndent();
                }
            }
            return Indent.getNoneIndent();
        }
    }
}
