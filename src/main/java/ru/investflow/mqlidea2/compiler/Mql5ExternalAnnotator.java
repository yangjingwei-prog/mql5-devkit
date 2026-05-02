package ru.investflow.mqlidea2.compiler;

import com.intellij.lang.annotation.AnnotationHolder;
import com.intellij.lang.annotation.ExternalAnnotator;
import com.intellij.lang.annotation.HighlightSeverity;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.settings.Mql5Settings;

import java.util.List;

/**
 * 将 MetaEditor 编译错误标注到编辑器中（红色/黄色下划线）
 * 编译后通过 Mql5CompilationResultCache 获取错误列表
 */
public class Mql5ExternalAnnotator extends ExternalAnnotator<Mql5ExternalAnnotator.Input, List<Mql5ErrorParser.Mql5Error>> {

    public static class Input {
        public final PsiFile file;
        public final String filePath;

        public Input(PsiFile file) {
            this.file = file;
            this.filePath = file.getVirtualFile() != null ? file.getVirtualFile().getCanonicalPath() : null;
        }
    }

    @Override
    public @Nullable Input collectInformation(@NotNull PsiFile file) {
        return new Input(file);
    }

    @Override
    public @Nullable List<Mql5ErrorParser.Mql5Error> doAnnotate(@Nullable Input input) {
        if (input == null || input.filePath == null) return null;

        Mql5Settings settings = Mql5Settings.getInstance();
        if (!settings.isErrorAnalysis()) return null;

        Mql5CompilationResultCache cache = Mql5CompilationResultCache.getInstance(input.file.getProject());
        List<Mql5ErrorParser.Mql5Error> errors = cache.get(input.filePath);
        return errors.isEmpty() ? null : errors;
    }

    @Override
    public void apply(@NotNull PsiFile file, @Nullable List<Mql5ErrorParser.Mql5Error> errors,
                      @NotNull AnnotationHolder holder) {
        if (errors == null || errors.isEmpty()) return;

        Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
        if (document == null) return;

        for (Mql5ErrorParser.Mql5Error err : errors) {
            int line = Math.max(0, err.line - 1);
            if (line >= document.getLineCount()) continue;

            int lineStart = document.getLineStartOffset(line);
            int lineEnd = document.getLineEndOffset(line);

            // Use column for precise error location
            int col = Math.max(0, err.column - 1);
            int start = Math.min(lineStart + col, lineEnd);
            int end = lineEnd;

            TextRange range = new TextRange(start, end);

            HighlightSeverity severity = err.isError() ? HighlightSeverity.ERROR : HighlightSeverity.WARNING;
            holder.newAnnotation(severity, err.message)
                .range(range)
                .create();
        }
    }
}
