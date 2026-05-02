package ru.investflow.mqlidea2.parser.parsing.statement;

import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;
import ru.investflow.mqlidea2.parser.parsing.util.ParsingUtils;
import ru.investflow.mqlidea2.parser.parsing.util.TokenAdvanceMode;
import ru.investflow.mqlidea2.psi.MQL4Elements;

import static com.intellij.lang.parser.GeneratedParserUtilBase.recursion_guard_;
import static ru.investflow.mqlidea2.parser.parsing.statement.VarDeclarationStatement.parseVarDeclaration;
import static ru.investflow.mqlidea2.parser.parsing.util.ParsingUtils.parseTokenOrFail;

public class StatementParsing implements MQL4Elements {

    private static final TokenSet SINGLE_WORD_STATEMENTS = TokenSet.create(
            BREAK_KEYWORD,
            CONTINUE_KEYWORD
    );

    public static boolean parseStatement(PsiBuilder b, int l) {
        //noinspection SimplifiableIfStatement
        if (!recursion_guard_(b, l, "parseStatement")) {
            return false;
        }

        return parseEmptyStatement(b)
                || parseVarDeclaration(b, l)
                || parseReturnStatement(b)
                || parseSingleWordStatement(b);
    }

    public static boolean parseSingleWordStatement(PsiBuilder b) {
        IElementType t = b.getTokenType();
        if (!SINGLE_WORD_STATEMENTS.contains(t)) {
            return false;
        }
        PsiBuilder.Marker m = b.mark();
        b.advanceLexer();
        if (!parseTokenOrFail(b, SEMICOLON)) {
            ParsingUtils.advanceLexerUntil(b, SEMICOLON, TokenAdvanceMode.ADVANCE);
        }
        m.done(SINGLE_WORD_STATEMENT);
        return true;
    }

    public static boolean parseReturnStatement(PsiBuilder b) {
        if (b.getTokenType() != RETURN_KEYWORD) {
            return false;
        }
        PsiBuilder.Marker m = b.mark();
        b.advanceLexer(); // 'return'
        // consume everything until semicolon
        while (!b.eof() && b.getTokenType() != SEMICOLON
                && b.getTokenType() != R_CURLY_BRACKET) {
            b.advanceLexer();
        }
        if (b.getTokenType() == SEMICOLON) {
            b.advanceLexer();
        }
        m.done(SINGLE_WORD_STATEMENT);
        return true;
    }

    public static boolean parseEmptyStatement(PsiBuilder b) {
        IElementType t = b.getTokenType();
        if (t != SEMICOLON) {
            return false;
        }
        PsiBuilder.Marker m = b.mark();
        b.advanceLexer();
        m.done(EMPTY_STATEMENT);
        return true;
    }

}
