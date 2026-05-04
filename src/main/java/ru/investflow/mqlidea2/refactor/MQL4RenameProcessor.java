package ru.investflow.mqlidea2.refactor;

import com.intellij.psi.PsiElement;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.psi.impl.MQL4ClassElement;
import ru.investflow.mqlidea2.psi.impl.MQL4FunctionElement;

import java.util.List;

/**
 * Rename processor for MQL4/MQL5 functions and classes.
 * Enables Shift+F6 (Rename) for function and class declarations.
 */
public class MQL4RenameProcessor extends RenamePsiElementProcessor {

    @Override
    public boolean canProcessElement(@NotNull PsiElement element) {
        return element instanceof MQL4FunctionElement || element instanceof MQL4ClassElement;
    }
}
