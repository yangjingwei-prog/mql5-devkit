package ru.investflow.mqlidea2.editor;

import com.intellij.openapi.editor.highlighter.EditorHighlighter;
import com.intellij.openapi.editor.highlighter.EditorHighlighterFactory;
import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CodeStyleSettingsCustomizable;
import com.intellij.psi.codeStyle.LanguageCodeStyleSettingsProvider;
import com.intellij.lang.Language;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4FileType;
import ru.investflow.mqlidea2.MQL4Language;

/**
 * Code style settings provider for MQL4/MQL5.
 * Settings > Editor > Code Style > MQL5.
 */
public class MQL4CodeStyleSettingsProvider extends LanguageCodeStyleSettingsProvider {

    @Override
    public @NotNull Language getLanguage() {
        return MQL4Language.INSTANCE;
    }

    @Override
    public void customizeSettings(@NotNull CodeStyleSettingsCustomizable consumer, @NotNull SettingsType settingsType) {
        if (settingsType == SettingsType.SPACING_SETTINGS) {
            consumer.showStandardOptions(
                    "SPACE_BEFORE_IF_PARENTHESES",
                    "SPACE_BEFORE_FOR_PARENTHESES",
                    "SPACE_BEFORE_WHILE_PARENTHESES",
                    "SPACE_AROUND_ASSIGNMENT_OPERATORS",
                    "SPACE_AROUND_LOGICAL_OPERATORS",
                    "SPACE_AROUND_EQUALITY_OPERATORS",
                    "SPACE_AROUND_RELATIONAL_OPERATORS",
                    "SPACE_AROUND_ADDITIVE_OPERATORS",
                    "SPACE_AROUND_MULTIPLICATIVE_OPERATORS",
                    "SPACE_AFTER_COMMA",
                    "SPACE_BEFORE_COMMA"
            );
        }
    }

    @Override
    public @Nullable String getCodeSample(@NotNull SettingsType settingsType) {
        return "// MQL5 Code Style Sample\n" +
                "#property copyright \"Copyright 2025\"\n" +
                "#property version   \"1.00\"\n" +
                "\n" +
                "input int    MAPeriod = 14;\n" +
                "input double LotSize  = 0.1;\n" +
                "\n" +
                "int OnInit() {\n" +
                "    Print(\"EA initialized\");\n" +
                "    return INIT_SUCCEEDED;\n" +
                "}\n" +
                "\n" +
                "void OnTick() {\n" +
                "    double ma = iMA(_Symbol, PERIOD_H1, MAPeriod,\n" +
                "                    MODE_SMA, PRICE_CLOSE, 0);\n" +
                "    if (ma > Close[0]) {\n" +
                "        OrderSend(_Symbol, OP_BUY, LotSize,\n" +
                "                  Ask, 3, 0, 0);\n" +
                "    }\n" +
                "}\n";
    }
}
