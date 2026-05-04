package ru.investflow.mqlidea2.index;

import com.intellij.lang.cacheBuilder.DefaultWordsScanner;
import com.intellij.lang.findUsages.FindUsagesProvider;
import com.intellij.lexer.FlexAdapter;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.tree.TokenSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4Lexer;
import ru.investflow.mqlidea2.parser.MQL4ParserDefinition;
import ru.investflow.mqlidea2.psi.MQL4Elements;
import ru.investflow.mqlidea2.psi.MQL4TokenSets;
import ru.investflow.mqlidea2.psi.impl.MQL4ClassElement;
import ru.investflow.mqlidea2.psi.impl.MQL4FunctionElement;

/**
 * Find Usages provider for MQL4/MQL5 functions and classes.
 * Enables Alt+F7 (Find Usages) for function and class declarations.
 */
public class MQL4FindUsagesProvider implements FindUsagesProvider {

    @Override
    public @NotNull DefaultWordsScanner getWordsScanner() {
        return new DefaultWordsScanner(
                new FlexAdapter(new MQL4Lexer(null)),
                TokenSet.create(MQL4Elements.IDENTIFIER),
                MQL4ParserDefinition.WHITE_SPACES,
                MQL4ParserDefinition.COMMENTS,
                TokenSet.EMPTY
        );
    }

    @Override
    public boolean canFindUsagesFor(@NotNull PsiElement psiElement) {
        return psiElement instanceof MQL4FunctionElement
                || psiElement instanceof MQL4ClassElement
                || psiElement instanceof PsiNamedElement;
    }

    @Override
    public @Nullable String getHelpId(@NotNull PsiElement psiElement) {
        return null;
    }

    @Override
    public @NotNull String getType(@NotNull PsiElement element) {
        if (element instanceof MQL4FunctionElement) {
            return "function";
        }
        if (element instanceof MQL4ClassElement) {
            return "class";
        }
        return "element";
    }

    @Override
    public @NotNull String getDescriptiveName(@NotNull PsiElement element) {
        if (element instanceof MQL4FunctionElement) {
            return ((MQL4FunctionElement) element).getFunctionName();
        }
        if (element instanceof MQL4ClassElement) {
            return ((MQL4ClassElement) element).getName();
        }
        return element.getText();
    }

    @Override
    public @NotNull String getNodeText(@NotNull PsiElement element, boolean useFullName) {
        if (element instanceof MQL4FunctionElement) {
            MQL4FunctionElement fn = (MQL4FunctionElement) element;
            return fn.getFunctionName() + "(" + fn.getSignature() + ")";
        }
        if (element instanceof MQL4ClassElement) {
            return ((MQL4ClassElement) element).getName();
        }
        return element.getText();
    }
}
