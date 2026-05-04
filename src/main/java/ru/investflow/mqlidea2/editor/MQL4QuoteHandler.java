package ru.investflow.mqlidea2.editor;

import com.intellij.codeInsight.editorActions.QuoteHandler;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.highlighter.HighlighterIterator;
import com.intellij.psi.tree.IElementType;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.psi.MQL4Elements;

/**
 * Quote handler for MQL4/MQL5.
 * Handles auto-closing of quotes and skipping over closing quotes.
 */
public class MQL4QuoteHandler implements QuoteHandler {

    @Override
    public boolean isClosingQuote(HighlighterIterator iterator, int offset) {
        IElementType tokenType = (IElementType) iterator.getTokenType();
        if (tokenType == MQL4Elements.STRING_LITERAL || tokenType == MQL4Elements.CHAR_LITERAL) {
            int start = iterator.getStart();
            int end = iterator.getEnd();
            return offset == end - 1 && end - start > 1;
        }
        return false;
    }

    @Override
    public boolean isOpeningQuote(HighlighterIterator iterator, int offset) {
        IElementType tokenType = (IElementType) iterator.getTokenType();
        if (tokenType == MQL4Elements.STRING_LITERAL || tokenType == MQL4Elements.CHAR_LITERAL) {
            int start = iterator.getStart();
            return offset == start;
        }
        return false;
    }

    @Override
    public boolean hasNonClosedLiteral(@NotNull Editor editor, @NotNull HighlighterIterator iterator, int offset) {
        return true;
    }

    @Override
    public boolean isInsideLiteral(@NotNull HighlighterIterator iterator) {
        IElementType tokenType = (IElementType) iterator.getTokenType();
        return tokenType == MQL4Elements.STRING_LITERAL ||
                tokenType == MQL4Elements.CHAR_LITERAL;
    }
}
