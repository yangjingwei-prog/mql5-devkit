package ru.investflow.mqlidea2.action;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4Icons;

import javax.swing.*;
import java.io.IOException;

/**
 * Module builder for creating new MQL5 projects with standard directory layout.
 */
public class Mql5ModuleBuilder extends ModuleBuilder {

    public static final String MODULE_TYPE_ID = "MQL5_MODULE";

    @Override
    public ModuleType<?> getModuleType() {
        return Mql5ModuleType.INSTANCE;
    }

    @Override
    public void setupRootModel(@NotNull ModifiableRootModel modifiableRootModel) throws ConfigurationException {
        VirtualFile contentRoot = modifiableRootModel.getContentRoots().length > 0
                ? modifiableRootModel.getContentRoots()[0]
                : null;

        if (contentRoot == null) {
            String path = getContentEntryPath();
            if (path == null) return;
            java.io.File dir = new java.io.File(path);
            if (!dir.exists() && !dir.mkdirs()) return;
            try {
                contentRoot = modifiableRootModel.getProject().getBaseDir();
            } catch (Exception e) {
                return;
            }
        }

        if (contentRoot == null) return;

        // Create standard MQL5 directory structure
        String[] dirs = {"Experts", "Indicators", "Scripts", "Include", "Files"};
        for (String dirName : dirs) {
            try {
                contentRoot.createChildDirectory(this, dirName);
            } catch (IOException ignored) {
            }
        }
    }

    /**
     * Custom ModuleType for MQL5 projects.
     */
    public static class Mql5ModuleType extends ModuleType<Mql5ModuleBuilder> {

        static final Mql5ModuleType INSTANCE = new Mql5ModuleType();

        private Mql5ModuleType() {
            super(MODULE_TYPE_ID);
        }

        @Override
        public @NotNull Mql5ModuleBuilder createModuleBuilder() {
            return new Mql5ModuleBuilder();
        }

        @Override
        public @NotNull String getName() {
            return "MQL5";
        }

        @Override
        public @NotNull String getDescription() {
            return "MQL5 project with standard directory structure (Experts, Indicators, Scripts, Include)";
        }

        @Override
        public @NotNull Icon getNodeIcon(@Deprecated boolean isOpened) {
            return MQL4Icons.File;
        }
    }
}
