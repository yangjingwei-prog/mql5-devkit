package ru.investflow.mqlidea2.editor;

import com.intellij.openapi.editor.DefaultLanguageHighlighterColors;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4FileType;

import javax.swing.Icon;
import java.util.Map;

/**
 * Color Settings Page for MQL4/MQL5 syntax highlighting.
 * Accessible via Settings > Editor > Color Scheme > MQL5
 */
public class MQL4ColorSettingsPage implements ColorSettingsPage {

    private static final AttributesDescriptor[] DESCRIPTORS = {
            new AttributesDescriptor("Keyword", DefaultLanguageHighlighterColors.KEYWORD),
            new AttributesDescriptor("String", DefaultLanguageHighlighterColors.STRING),
            new AttributesDescriptor("Number", DefaultLanguageHighlighterColors.NUMBER),
            new AttributesDescriptor("Operator", DefaultLanguageHighlighterColors.OPERATION_SIGN),
            new AttributesDescriptor("Line comment", DefaultLanguageHighlighterColors.LINE_COMMENT),
            new AttributesDescriptor("Block comment", DefaultLanguageHighlighterColors.BLOCK_COMMENT),
            new AttributesDescriptor("Identifier", DefaultLanguageHighlighterColors.IDENTIFIER),
            new AttributesDescriptor("Built-in function", DefaultLanguageHighlighterColors.STATIC_METHOD),
            new AttributesDescriptor("Built-in constant", DefaultLanguageHighlighterColors.CONSTANT),
            new AttributesDescriptor("Preprocessor directive", DefaultLanguageHighlighterColors.METADATA),
    };

    private static final String DEMO_TEXT =
            "// MQL5 Demo\n" +
            "#property copyright \"Test\"\n" +
            "#property version   \"1.00\"\n" +
            "#include <Trade\\Trade.mqh>\n" +
            "\n" +
            "input int    MAPeriod = 14;\n" +
            "input double LotSize  = 0.1;\n" +
            "\n" +
            "int OnInit()\n" +
            "  {\n" +
            "   Print(\"Hello MQL5\");\n" +
            "   return INIT_SUCCEEDED;\n" +
            "  }\n" +
            "\n" +
            "void OnTick()\n" +
            "  {\n" +
            "   double ma = iMA(_Symbol, PERIOD_CURRENT, MAPeriod,\n" +
            "                   0, MODE_SMA, PRICE_CLOSE);\n" +
            "   if(OrdersTotal() == 0)\n" +
            "     {\n" +
            "      OrderSend(_Symbol, OP_BUY, LotSize,\n" +
            "                SymbolInfoDouble(_Symbol, SYMBOL_ASK),\n" +
            "                3, 0, 0);\n" +
            "     }\n" +
            "  }\n";

    @Override
    public @Nullable Icon getIcon() {
        return MQL4FileType.INSTANCE.getIcon();
    }

    @Override
    public @NotNull SyntaxHighlighter getHighlighter() {
        return new MQL4SyntaxHighlighter();
    }

    @Override
    public @NotNull String getDemoText() {
        return DEMO_TEXT;
    }

    @Override
    public @Nullable Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return null;
    }

    @Override
    public @NotNull AttributesDescriptor[] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    public @NotNull ColorDescriptor[] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    public @NotNull String getDisplayName() {
        return "MQL5";
    }
}
