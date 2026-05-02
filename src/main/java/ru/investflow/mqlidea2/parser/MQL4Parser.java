package ru.investflow.mqlidea2.parser;

import com.intellij.lang.ASTNode;
import com.intellij.lang.LighterASTNode;
import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiParser;
import com.intellij.psi.tree.IElementType;
import com.intellij.util.diff.FlyweightCapableTreeStructure;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.psi.MQL4Elements;
import ru.investflow.mqlidea2.psi.MQL4TokenSets;

import static ru.investflow.mqlidea2.parser.parsing.BracketBlockParsing.parseBracketsBlock;
import static ru.investflow.mqlidea2.parser.parsing.ClassParsing.parseClassOrStruct;
import static ru.investflow.mqlidea2.parser.parsing.CommentParsing.parseComment;
import static ru.investflow.mqlidea2.parser.parsing.FunctionsParsing.parseFunction;
import static ru.investflow.mqlidea2.parser.parsing.preprocessor.PreprocessorParsing.parsePreprocessorBlock;
import static ru.investflow.mqlidea2.parser.parsing.statement.EnumParsing.parseEnum;
import static ru.investflow.mqlidea2.parser.parsing.util.ParsingErrors.error;

public class MQL4Parser implements PsiParser, MQL4Elements {

    private static final com.intellij.psi.tree.TokenSet GLOBAL_VAR_PREFIXES =
            com.intellij.psi.tree.TokenSet.create(INPUT_KEYWORD, SINPUT_KEYWORD, EXTERN_KEYWORD, STATIC_KEYWORD, CONST_KEYWORD, VIRTUAL_KEYWORD);

    @NotNull
    public ASTNode parse(@NotNull IElementType root, @NotNull PsiBuilder b) {
        doParse(root, b);
        return b.getTreeBuilt();
    }

    @NotNull
    public FlyweightCapableTreeStructure<LighterASTNode> parseLight(IElementType root, PsiBuilder builder) {
        doParse(root, builder);
        return builder.getLightTree();
    }

    private void doParse(@NotNull IElementType root, @NotNull PsiBuilder b) {
        PsiBuilder.Marker fileBlock = b.mark();
        while (!b.eof()) {
            boolean r = parseComment(b)
                    || parsePreprocessorBlock(b)
                    || parseInputGroup(b)
                    || parseGlobalVarDeclaration(b)
                    || parseFunction(b)
                    || parseEnum(b, 0)
                    || parseClassOrStruct(b, 0)
                    || parseBracketsBlock(b, 0);
            if (!r) {
                PsiBuilder.Marker errorMark = b.mark();
                b.advanceLexer();
                errorMark.error("Unexpected token");
            }
        }
        fileBlock.done(root);
    }

    /**
     * Parse MQL5 input group declaration: input group "group name"
     * The group keyword is not a reserved word, it's just an identifier after 'input'.
     */
    private boolean parseInputGroup(PsiBuilder b) {
        if (b.getTokenType() != INPUT_KEYWORD) {
            return false;
        }
        // Check if this is "input group" pattern: INPUT_KEYWORD IDENTIFIER("group") STRING_LITERAL
        if (b.lookAhead(1) != IDENTIFIER || b.lookAhead(2) != STRING_LITERAL) {
            return false;
        }
        // Verify the identifier text is "group"
        String text = b.getTokenText(); // save current token text won't help, we need lookAhead text
        // Use the builder's original text to check
        int start = b.getCurrentOffset();
        // advance past 'input' to check identifier text
        PsiBuilder.Marker m = b.mark();
        b.advanceLexer(); // 'input'
        String idText = b.getTokenText();
        if (!"group".equals(idText)) {
            // rollback - this is a regular input variable declaration like "input groupType varName"
            m.rollbackTo();
            return false;
        }
        b.advanceLexer(); // 'group' identifier
        b.advanceLexer(); // string literal
        m.done(INPUT_GROUP_BLOCK);
        return true;
    }

    /**
     * Parse global variable declarations with optional prefixes: input, extern, static, const
     * Form: [PREFIX] TYPE IDENTIFIER [= EXPR] [, IDENTIFIER [= EXPR]]* ;
     * TYPE can be a built-in type (int, double, ...) or a custom type (IDENTIFIER like ENUM_LINE_STYLE)
     */
    private boolean parseGlobalVarDeclaration(PsiBuilder b) {
        int n = 0;
        // optional prefix: input, extern, static, const
        if (GLOBAL_VAR_PREFIXES.contains(b.lookAhead(n))) {
            n++;
        }
        // type: built-in or custom (IDENTIFIER)
        IElementType typeToken = b.lookAhead(n);
        if (typeToken != IDENTIFIER && !MQL4TokenSets.DATA_TYPES.contains(typeToken)) {
            return false;
        }
        // must be followed by an identifier (variable name)
        if (b.lookAhead(n + 1) != IDENTIFIER) {
            return false;
        }
        // distinguish from function: if followed by '(' it's a function, not a variable
        if (b.lookAhead(n + 2) == L_ROUND_BRACKET) {
            return false;
        }

        PsiBuilder.Marker m = b.mark();
        // consume prefix
        if (n > 0) {
            b.advanceLexer();
        }
        // consume type
        b.advanceLexer();
        // consume identifier and optional assignment
        while (true) {
            if (b.getTokenType() != IDENTIFIER) {
                break;
            }
            b.advanceLexer(); // identifier

            // optional array brackets
            if (b.getTokenType() == L_SQUARE_BRACKET) {
                b.advanceLexer();
                if (b.getTokenType() == INTEGER_LITERAL || b.getTokenType() == IDENTIFIER) {
                    b.advanceLexer();
                }
                if (b.getTokenType() == R_SQUARE_BRACKET) {
                    b.advanceLexer();
                }
            }

            // optional assignment
            if (b.getTokenType() == EQ) {
                b.advanceLexer(); // '='
                // consume expression until comma or semicolon
                while (!b.eof()
                        && b.getTokenType() != SEMICOLON
                        && b.getTokenType() != COMMA) {
                    b.advanceLexer();
                }
            }

            if (b.getTokenType() == SEMICOLON) {
                b.advanceLexer();
                break;
            }
            if (b.getTokenType() == COMMA) {
                b.advanceLexer();
                continue;
            }
            break;
        }
        m.done(GLOBAL_VAR_DECLARATION);
        return true;
    }
}
