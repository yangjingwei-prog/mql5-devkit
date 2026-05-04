package ru.investflow.mqlidea2.editor;

import com.intellij.ide.FileIconProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4Icons;

import javax.swing.Icon;

/**
 * Provides different file icons for MQL5 file types.
 */
public class MQL4FileIconProvider implements FileIconProvider {

    @Override
    public @Nullable Icon getIcon(@NotNull VirtualFile file, int flags, @Nullable Project project) {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".mqh")) {
            return MQL4Icons.MQLHeader;
        }
        if (name.endsWith(".mq5") || name.endsWith(".mq4") || name.endsWith(".mql5") || name.endsWith(".mql4")) {
            return MQL4Icons.File;
        }
        return null;
    }
}
