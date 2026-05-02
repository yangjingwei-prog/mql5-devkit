package ru.investflow.mqlidea2.settings;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import org.jetbrains.annotations.NotNull;

/**
 * MQL5 DevKit 持久化配置
 * 替代原有的 MQL4PluginSettingsImpl（只有2个选项）
 */
@State(name = "Mql5DevKitSettings", storages = @Storage("mql5-devkit.xml"))
public class Mql5Settings implements PersistentStateComponent<Mql5Settings.State> {

    public static class State {
        public String metaEditorPath = "";
        public String terminalPath = "";
        public String mql5DataDir = "";
        public boolean compileOnSave = false;
        public boolean showNotification = true;
        public boolean autoDeploy = false;
        public boolean enableClangd = false;
        public String clangdPath = "";
        public boolean enDocs = true;
        public boolean errorAnalysis = true;
    }

    private State state = new State();

    public static Mql5Settings getInstance() {
        return ApplicationManager.getApplication().getService(Mql5Settings.class);
    }

    @Override
    public @NotNull State getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
    }

    public String getMetaEditorPath() { return state.metaEditorPath; }
    public void setMetaEditorPath(String v) { state.metaEditorPath = v; }

    public String getTerminalPath() { return state.terminalPath; }
    public void setTerminalPath(String v) { state.terminalPath = v; }

    public String getMql5DataDir() { return state.mql5DataDir; }
    public void setMql5DataDir(String v) { state.mql5DataDir = v; }

    public boolean isCompileOnSave() { return state.compileOnSave; }
    public void setCompileOnSave(boolean v) { state.compileOnSave = v; }

    public boolean isShowNotification() { return state.showNotification; }
    public void setShowNotification(boolean v) { state.showNotification = v; }

    public boolean isAutoDeploy() { return state.autoDeploy; }
    public void setAutoDeploy(boolean v) { state.autoDeploy = v; }

    public boolean isEnableClangd() { return state.enableClangd; }
    public void setEnableClangd(boolean v) { state.enableClangd = v; }

    public String getClangdPath() { return state.clangdPath; }
    public void setClangdPath(String v) { state.clangdPath = v; }

    public boolean isEnDocs() { return state.enDocs; }
    public void setEnDocs(boolean v) { state.enDocs = v; }

    public boolean isErrorAnalysis() { return state.errorAnalysis; }
    public void setErrorAnalysis(boolean v) { state.errorAnalysis = v; }
}
