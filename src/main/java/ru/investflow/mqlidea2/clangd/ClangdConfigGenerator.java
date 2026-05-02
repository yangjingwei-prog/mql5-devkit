package ru.investflow.mqlidea2.clangd;

import org.jetbrains.annotations.NotNull;
import ru.investflow.mqlidea2.settings.Mql5Settings;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

/**
 * 生成 .clangd 配置文件，让 clangd 将 MQL5 当作 C++ 处理
 */
public class ClangdConfigGenerator {

    /**
     * 生成 .clangd 配置内容
     */
    @NotNull
    public static String generate(@NotNull Mql5Settings settings) {
        StringBuilder sb = new StringBuilder();
        sb.append("CompileFlags:\n");
        sb.append("  Add:\n");
        sb.append("    - -xc++\n");
        sb.append("    - -std=c++17\n");
        sb.append("    - -DMQL5\n");
        sb.append("    - -D__MQL5__\n");

        // 添加 Include 路径
        String dataDir = settings.getMql5DataDir();
        if (dataDir != null && !dataDir.isEmpty()) {
            sb.append("    - -I").append(dataDir.replace("\\", "/")).append("/Include\n");
            sb.append("    - -I").append(dataDir.replace("\\", "/")).append("/Include/Trade\n");
            sb.append("    - -I").append(dataDir.replace("\\", "/")).append("/Include/Experts\n");
            sb.append("    - -I").append(dataDir.replace("\\", "/")).append("/Include/Indicators\n");
        }

        sb.append("  Remove:\n");
        sb.append("    - -W*\n");
        sb.append("\n");
        sb.append("Diagnostics:\n");
        sb.append("  Suppress:\n");

        List<String> suppressions = Arrays.asList(
            "pp_file_not_found",
            "unknown_typename",
            "nondeductible_auto",
            "drangen",
            "variadic_device",
            "extension_used",
            "gcc_diag",
            "poison"
        );

        for (String s : suppressions) {
            sb.append("    - ").append(s).append("\n");
        }

        sb.append("  UnusedIncludes: None\n");
        sb.append("  ClangTidy:\n");
        sb.append("    Remove:\n");
        sb.append("      - bugprone-*\n");
        sb.append("      - modernize-*\n");
        sb.append("      - readability-*\n");
        sb.append("\n");
        sb.append("InlayHints:\n");
        sb.append("  Enabled: Yes\n");
        sb.append("  ParameterNames: Yes\n");
        sb.append("  DeducedTypes: Yes\n");

        return sb.toString();
    }

    /**
     * 将配置写入项目根目录的 .clangd 文件
     */
    public static boolean writeToFile(@NotNull String projectBasePath, @NotNull Mql5Settings settings) {
        String content = generate(settings);
        try {
            Path path = Path.of(projectBasePath, ".clangd");
            Files.writeString(path, content);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
