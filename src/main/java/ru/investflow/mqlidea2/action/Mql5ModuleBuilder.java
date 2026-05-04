package ru.investflow.mqlidea2.action;

import com.intellij.ide.util.projectWizard.ModuleBuilder;
import com.intellij.ide.util.projectWizard.ModuleWizardStep;
import com.intellij.ide.util.projectWizard.WizardContext;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.module.ModuleTypeManager;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import ru.investflow.mqlidea2.MQL4Icons;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Year;

/**
 * Module builder for creating new MQL5 projects with standard directory layout
 * and optional starter file from template.
 */
public class Mql5ModuleBuilder extends ModuleBuilder {

    public static final String MODULE_TYPE_ID = "MQL5_MODULE";

    private String templateType = "ExpertAdvisor";
    private String templateName = "MyExpert";

    @Override
    public ModuleType<?> getModuleType() {
        return Mql5ModuleType.INSTANCE;
    }

    @Override
    public ModuleWizardStep modifySettingsStep(@NotNull com.intellij.ide.util.projectWizard.SettingsStep settingsStep) {
        return new ModuleWizardStep() {
            private final JPanel panel = new JPanel(new BorderLayout(10, 10));
            private final JRadioButton eaBtn = new JRadioButton("Expert Advisor", true);
            private final JRadioButton indBtn = new JRadioButton("Indicator");
            private final JRadioButton scriptBtn = new JRadioButton("Script");
            private final JRadioButton serviceBtn = new JRadioButton("Service");
            private final JRadioButton noneBtn = new JRadioButton("None (empty project)");
            private final JTextField nameField = new JTextField("MyExpert", 20);

            {
                JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                typePanel.add(new JLabel("Initial file type:"));
                ButtonGroup bg = new ButtonGroup();
                bg.add(eaBtn); bg.add(indBtn); bg.add(scriptBtn); bg.add(serviceBtn); bg.add(noneBtn);
                typePanel.add(eaBtn); typePanel.add(indBtn); typePanel.add(scriptBtn);
                typePanel.add(serviceBtn); typePanel.add(noneBtn);

                JPanel namePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
                namePanel.add(new JLabel("File name:"));
                namePanel.add(nameField);

                panel.add(typePanel, BorderLayout.NORTH);
                panel.add(namePanel, BorderLayout.CENTER);

                eaBtn.addActionListener(e -> updateDefaultName());
                indBtn.addActionListener(e -> updateDefaultName());
                scriptBtn.addActionListener(e -> updateDefaultName());
                serviceBtn.addActionListener(e -> updateDefaultName());
            }

            private void updateDefaultName() {
                if (eaBtn.isSelected()) nameField.setText("MyExpert");
                else if (indBtn.isSelected()) nameField.setText("MyIndicator");
                else if (scriptBtn.isSelected()) nameField.setText("MyScript");
                else if (serviceBtn.isSelected()) nameField.setText("MyService");
            }

            @Override
            public JComponent getComponent() {
                return panel;
            }

            @Override
            public void updateDataModel() {
                if (noneBtn.isSelected()) {
                    templateType = "None";
                } else if (eaBtn.isSelected()) {
                    templateType = "ExpertAdvisor";
                } else if (indBtn.isSelected()) {
                    templateType = "Indicator";
                } else if (scriptBtn.isSelected()) {
                    templateType = "Script";
                } else if (serviceBtn.isSelected()) {
                    templateType = "Service";
                }
                templateName = nameField.getText().trim();
                if (templateName.isEmpty()) templateName = "MyMQL5";
            }

            @Override
            public boolean validate() throws ConfigurationException {
                return true;
            }
        };
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

        // Create initial file from template
        if (!"None".equals(templateType)) {
            createTemplateFile(contentRoot);
        }
    }

    private void createTemplateFile(@NotNull VirtualFile contentRoot) {
        String templateFile;
        String targetDir;
        String ext = "mq5";

        switch (templateType) {
            case "ExpertAdvisor":
                templateFile = "MQL5 Expert Advisor.mq5.ft";
                targetDir = "Experts";
                break;
            case "Indicator":
                templateFile = "MQL5 Indicator.mq5.ft";
                targetDir = "Indicators";
                break;
            case "Script":
                templateFile = "MQL5 Script.mq5.ft";
                targetDir = "Scripts";
                break;
            case "Service":
                templateFile = "MQL5 Service.mq5.ft";
                targetDir = "Scripts";
                break;
            default:
                return;
        }

        VirtualFile dir = contentRoot.findChild(targetDir);
        if (dir == null) return;

        String resourcePath = "/fileTemplates/internal/" + templateFile;
        try (InputStream is = getClass().getResourceAsStream(resourcePath)) {
            if (is == null) return;
            String content = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            content = content.replace("${NAME}", templateName);
            content = content.replace("${YEAR}", String.valueOf(Year.now().getValue()));
            content = content.replace("${USER}", System.getProperty("user.name", ""));

            String fileName = templateName + "." + ext;
            VirtualFile file = dir.createChildData(this, fileName);
            file.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
        } catch (IOException ignored) {
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
