package ru.investflow.mqlidea2.editor;

import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.spellchecker.tokenizer.SpellcheckingStrategy;
import com.intellij.spellchecker.tokenizer.Tokenizer;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.psi.MQL4Elements;

/**
 * Spellchecker strategy for MQL4/MQL5.
 * Only checks string literals and comments, skips identifiers/keywords.
 */
public class MQL4SpellcheckingStrategy extends SpellcheckingStrategy {

    @Override
    public @NotNull Tokenizer<?> getTokenizer(PsiElement element) {
        IElementType type = element.getNode().getElementType();
        if (type == MQL4Elements.STRING_LITERAL || type == MQL4Elements.CHAR_LITERAL) {
            return TEXT_TOKENIZER;
        }
        if (type == MQL4Elements.LINE_COMMENT || type == MQL4Elements.BLOCK_COMMENT) {
            return TEXT_TOKENIZER;
        }
        return EMPTY_TOKENIZER;
    }
}
