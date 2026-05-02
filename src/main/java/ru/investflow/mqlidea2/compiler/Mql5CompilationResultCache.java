package ru.investflow.mqlidea2.compiler;

import com.intellij.openapi.components.Service;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Project-level cache for MQL5 compilation results.
 * Stores the latest compilation errors per file path,
 * so that ExternalAnnotator can display them in the editor.
 */
@Service(Service.Level.PROJECT)
public final class Mql5CompilationResultCache {

    private final Map<String, List<Mql5ErrorParser.Mql5Error>> cache = new ConcurrentHashMap<>();

    public static Mql5CompilationResultCache getInstance(@NotNull Project project) {
        return project.getService(Mql5CompilationResultCache.class);
    }

    public void put(@NotNull String filePath, @NotNull List<Mql5ErrorParser.Mql5Error> errors) {
        cache.put(filePath, errors);
    }

    @NotNull
    public List<Mql5ErrorParser.Mql5Error> get(@NotNull String filePath) {
        return cache.getOrDefault(filePath, Collections.emptyList());
    }

    public void clear(@NotNull String filePath) {
        cache.remove(filePath);
    }

    public void clearAll() {
        cache.clear();
    }
}
