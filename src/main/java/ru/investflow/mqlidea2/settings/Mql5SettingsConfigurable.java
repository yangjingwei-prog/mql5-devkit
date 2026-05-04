package ru.investflow.mqlidea2.settings;

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.Nullable;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import java.io.File;

/**
 * MQL5 DevKit 设置界面
 * Settings > Tools > MQL5 DevKit
 */
public class Mql5SettingsConfigurable implements Configurable {

    private TextFieldWithBrowseButton metaEditorPathField;
    private TextFieldWithBrowseButton terminalPathField;
    private TextFieldWithBrowseButton mql5DataDirField;
    private JBCheckBox compileOnSaveCheckBox;
    private JBCheckBox showNotificationCheckBox;
    private JBCheckBox autoDeployCheckBox;
    private JBCheckBox enableClangdCheckBox;
    private TextFieldWithBrowseButton clangdPathField;
    private JBCheckBox enDocsCheckBox;

    @Nls(capitalization = Nls.Capitalization.Title)
    @Override
    public String getDisplayName() {
        return "MQL5 DevKit";
    }

    @Nullable
    @Override
    public JComponent createComponent() {
        metaEditorPathField = new TextFieldWithBrowseButton();
        metaEditorPathField.addBrowseFolderListener(
            "Select MetaEditor", "Choose metaeditor64.exe",
            null, FileChooserDescriptorFactory.createSingleFileDescriptor("exe"));

        JButton autoDetectBtn = new JButton("Auto Detect");
        autoDetectBtn.addActionListener(e -> {
            String path = Mql5PathDetector.detectMetaEditor();
            if (path != null) {
                metaEditorPathField.setText(path);
                // 同时检测数据目录
                String dataDir = Mql5PathDetector.detectMql5DataDir();
                if (dataDir != null) {
                    mql5DataDirField.setText(dataDir);
                }
            }
        });

        terminalPathField = new TextFieldWithBrowseButton();
        terminalPathField.addBrowseFolderListener(
            "Select Terminal", "Choose terminal64.exe",
            null, FileChooserDescriptorFactory.createSingleFileDescriptor("exe"));

        mql5DataDirField = new TextFieldWithBrowseButton();
        mql5DataDirField.addBrowseFolderListener(
            "Select MQL5 Data Directory", "Choose the MQL5 data folder",
            null, FileChooserDescriptorFactory.createSingleFolderDescriptor());

        compileOnSaveCheckBox = new JBCheckBox("Compile on save");
        showNotificationCheckBox = new JBCheckBox("Show compilation result notification");
        autoDeployCheckBox = new JBCheckBox("Auto-deploy compiled files to terminal");

        enableClangdCheckBox = new JBCheckBox("Enable clangd IntelliSense");
        clangdPathField = new TextFieldWithBrowseButton();
        clangdPathField.addBrowseFolderListener(
            "Select clangd", "Choose clangd executable",
            null, FileChooserDescriptorFactory.createSingleFileDescriptor("exe"));

        enDocsCheckBox = new JBCheckBox("Use English documentation");

        JButton testBtn = new JButton("Test");
        testBtn.addActionListener(e -> {
            String path = metaEditorPathField.getText().trim();
            if (path.isEmpty()) {
                JOptionPane.showMessageDialog(null, "MetaEditor path is empty.", "Test Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            File exe = new File(path);
            if (!exe.exists()) {
                JOptionPane.showMessageDialog(null, "File not found:\n" + path, "Test Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (!exe.getName().toLowerCase().startsWith("metaeditor")) {
                JOptionPane.showMessageDialog(null, "File does not appear to be MetaEditor:\n" + exe.getName(), "Test Failed", JOptionPane.WARNING_MESSAGE);
                return;
            }
            try {
                ProcessBuilder pb = new ProcessBuilder(path, "/?");
                pb.redirectErrorStream(true);
                Process proc = pb.start();
                proc.waitFor();
                // metaeditor64.exe exits even with /? — just verify it launched
                JOptionPane.showMessageDialog(null, "MetaEditor found and executable.\n" + path, "Test OK", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(null, "Failed to launch MetaEditor:\n" + ex.getMessage(), "Test Failed", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel metaEditorPanel = new JPanel(new java.awt.BorderLayout(5, 0));
        metaEditorPanel.add(metaEditorPathField, java.awt.BorderLayout.CENTER);
        JPanel btnPanel = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 5, 0));
        btnPanel.add(autoDetectBtn);
        btnPanel.add(testBtn);
        metaEditorPanel.add(btnPanel, java.awt.BorderLayout.EAST);

        return FormBuilder.createFormBuilder()
            .addSeparator()
            .addLabeledComponent("MetaEditor Path:", metaEditorPanel)
            .addLabeledComponent("Terminal Path:", terminalPathField)
            .addLabeledComponent("MQL5 Data Directory:", mql5DataDirField)
            .addSeparator()
            .addComponent(compileOnSaveCheckBox)
            .addComponent(showNotificationCheckBox)
            .addComponent(autoDeployCheckBox)
            .addSeparator()
            .addComponent(enableClangdCheckBox)
            .addLabeledComponent("clangd Path:", clangdPathField)
            .addSeparator()
            .addComponent(enDocsCheckBox)
            .addComponentFillVertically(new JPanel(), 0)
            .getPanel();
    }

    @Override
    public boolean isModified() {
        Mql5Settings s = Mql5Settings.getInstance();
        return !metaEditorPathField.getText().equals(s.getMetaEditorPath())
            || !terminalPathField.getText().equals(s.getTerminalPath())
            || !mql5DataDirField.getText().equals(s.getMql5DataDir())
            || compileOnSaveCheckBox.isSelected() != s.isCompileOnSave()
            || showNotificationCheckBox.isSelected() != s.isShowNotification()
            || autoDeployCheckBox.isSelected() != s.isAutoDeploy()
            || enableClangdCheckBox.isSelected() != s.isEnableClangd()
            || !clangdPathField.getText().equals(s.getClangdPath())
            || enDocsCheckBox.isSelected() != s.isEnDocs();
    }

    @Override
    public void apply() throws ConfigurationException {
        Mql5Settings s = Mql5Settings.getInstance();
        s.setMetaEditorPath(metaEditorPathField.getText());
        s.setTerminalPath(terminalPathField.getText());
        s.setMql5DataDir(mql5DataDirField.getText());
        s.setCompileOnSave(compileOnSaveCheckBox.isSelected());
        s.setShowNotification(showNotificationCheckBox.isSelected());
        s.setAutoDeploy(autoDeployCheckBox.isSelected());
        s.setEnableClangd(enableClangdCheckBox.isSelected());
        s.setClangdPath(clangdPathField.getText());
        s.setEnDocs(enDocsCheckBox.isSelected());
    }

    @Override
    public void reset() {
        Mql5Settings s = Mql5Settings.getInstance();
        metaEditorPathField.setText(s.getMetaEditorPath());
        terminalPathField.setText(s.getTerminalPath());
        mql5DataDirField.setText(s.getMql5DataDir());
        compileOnSaveCheckBox.setSelected(s.isCompileOnSave());
        showNotificationCheckBox.setSelected(s.isShowNotification());
        autoDeployCheckBox.setSelected(s.isAutoDeploy());
        enableClangdCheckBox.setSelected(s.isEnableClangd());
        clangdPathField.setText(s.getClangdPath());
        enDocsCheckBox.setSelected(s.isEnDocs());
    }
}
