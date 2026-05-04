package ru.investflow.mqlidea2.editor;

import com.intellij.codeInsight.template.TemplateActionContext;
import com.intellij.codeInsight.template.TemplateContextType;
import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.MQL4Language;

/**
 * Live Template 上下文：仅在 MQL4/MQL5 文件中激活。
 */
public class MQL4LiveTemplateContext extends TemplateContextType {

    protected MQL4LiveTemplateContext() {
        super("MQL4", "MQL5");
    }

    @Override
    public boolean isInContext(@NotNull TemplateActionContext templateActionContext) {
        return templateActionContext.getFile().getLanguage().isKindOf(MQL4Language.INSTANCE);
    }
}
