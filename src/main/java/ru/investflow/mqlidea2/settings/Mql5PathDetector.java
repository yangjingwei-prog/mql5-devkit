package ru.investflow.mqlidea2.settings;

import com.intellij.openapi.util.SystemInfo;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * 自动检测 MetaTrader 5 安装路径
 * 扫描 Windows 注册表和常见安装目录
 */
public class Mql5PathDetector {

    /**
     * 自动检测 MetaEditor 路径
     */
    @Nullable
    public static String detectMetaEditor() {
        if (!SystemInfo.isWindows) return null;

        List<String> candidates = new ArrayList<>();

        // 1. 扫描常见安装目录
        String[] bases = {
            System.getenv("ProgramFiles"),
            System.getenv("ProgramFiles(x86)"),
            System.getProperty("user.home") + "\\AppData\\Roaming"
        };

        for (String base : bases) {
            if (base == null) continue;
            File dir = new File(base);
            if (!dir.exists()) continue;

            File[] files = dir.listFiles((d, name) ->
                name.toLowerCase().contains("metatrader") || name.toLowerCase().contains("mt5"));

            if (files != null) {
                for (File mtDir : files) {
                    candidates.add(new File(mtDir, "metaeditor64.exe").getAbsolutePath());
                }
            }
        }

        // 2. 扫描用户的 MetaQuotes 目录
        String appData = System.getenv("APPDATA");
        if (appData != null) {
            File metaquotes = new File(appData, "MetaQuotes");
            if (metaquotes.exists()) {
                File[] terminals = metaquotes.listFiles((d, name) ->
                    d.isDirectory() && new File(d, "metaeditor64.exe").exists());
                if (terminals != null) {
                    for (File t : terminals) {
                        candidates.add(new File(t, "metaeditor64.exe").getAbsolutePath());
                    }
                }
            }
        }

        // 返回第一个存在的路径
        for (String path : candidates) {
            if (new File(path).exists()) {
                return path;
            }
        }

        return null;
    }

    /**
     * 自动检测 MQL5 数据目录
     */
    @Nullable
    public static String detectMql5DataDir() {
        if (!SystemInfo.isWindows) return null;

        String appData = System.getenv("APPDATA");
        if (appData == null) return null;

        Path metaquotes = Paths.get(appData, "MetaQuotes", "Terminal");
        if (!Files.exists(metaquotes)) return null;

        try {
            File[] dirs = metaquotes.toFile().listFiles(File::isDirectory);
            if (dirs != null) {
                for (File dir : dirs) {
                    File mql5 = new File(dir, "MQL5");
                    if (mql5.exists()) {
                        return mql5.getAbsolutePath();
                    }
                }
            }
        } catch (Exception e) {
            // ignore
        }

        return null;
    }
}
