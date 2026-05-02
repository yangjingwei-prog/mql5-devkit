package ru.investflow.mqlidea2.compiler;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 解析 MetaEditor 编译输出日志
 * 格式: file.mq5(line,col): error/warning code: message
 */
public class Mql5ErrorParser {

    public static class Mql5Error {
        public final int line;
        public final int column;
        public final String severity; // "error" or "warning"
        public final String code;
        public final String message;

        public Mql5Error(int line, int column, String severity, String code, String message) {
            this.line = line;
            this.column = column;
            this.severity = severity;
            this.code = code;
            this.message = message;
        }

        public boolean isError() {
            return "error".equalsIgnoreCase(severity);
        }

        public boolean isWarning() {
            return "warning".equalsIgnoreCase(severity);
        }

        @Override
        public String toString() {
            return String.format("(%d,%d): %s %s: %s", line, column, severity, code, message);
        }
    }

    // MetaEditor log format: file.mq5(line,col): error code: message
    private static final Pattern ERROR_PATTERN = Pattern.compile(
        "[^\\(]+\\((\\d+),(\\d+)\\)\\s*:\\s*(error|warning)\\s+(\\d+)\\s*:\\s*(.*)"
    );

    /**
     * 解析 MetaEditor 编译日志文本
     */
    @NotNull
    public static List<Mql5Error> parseLog(@NotNull String logText) {
        List<Mql5Error> errors = new ArrayList<>();
        for (String line : logText.split("\n")) {
            line = line.trim();
            if (line.isEmpty()) continue;

            Matcher m = ERROR_PATTERN.matcher(line);
            if (m.find()) {
                errors.add(new Mql5Error(
                    Integer.parseInt(m.group(1)),
                    Integer.parseInt(m.group(2)),
                    m.group(3),
                    m.group(4),
                    m.group(5).trim()
                ));
            }
        }
        return errors;
    }

    /**
     * 统计错误和警告数量
     */
    public static String getSummary(@NotNull List<Mql5Error> errors) {
        long errorCount = errors.stream().filter(Mql5Error::isError).count();
        long warningCount = errors.stream().filter(Mql5Error::isWarning).count();
        return String.format("%d error(s), %d warning(s)", errorCount, warningCount);
    }
}
