package com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.decorator_actions;

import com.github.kornilova_l.flamegraph.configuration.Configuration;
import com.github.kornilova_l.flamegraph.configuration.MethodConfig;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.ConfigCheckboxTree;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MethodFormManager;
import com.github.kornilova_l.flamegraph.plugin.ui.config_dialog.method_form.MyTableView;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;


public class DialogHelper {
    private static final String cardKey = "new-table";

    static JPanel createAndShowTable(JPanel paramTableCards, List<MethodConfig.Parameter> parameters, ConfigCheckboxTree.TreeType treeType) {
        JPanel tablePanel = MyTableView.createTablePanel(parameters, treeType);
        paramTableCards.add(tablePanel, cardKey);
        ((CardLayout) paramTableCards.getLayout()).show(paramTableCards, cardKey);
        return tablePanel;
    }

    public static void saveConfig(@NotNull String className,
                                  @NotNull String methodName,
                                  boolean saveRetVal,
                                  @NotNull List<MethodConfig.Parameter> parameters,
                                  ConfigCheckboxTree tree,
                                  Configuration tempConfig) {
        MethodConfig methodConfig = new MethodConfig(
                className,
                methodName,
                parameters,
                true,
                saveRetVal
        );
        List<MethodConfig> methodConfigs;
        switch (tree.treeType) {
            case INCLUDING:
                tempConfig.addMethodConfig(methodConfig, false);
                methodConfigs = tempConfig.getIncludingMethodConfigs();
                break;
            case EXCLUDING:
                tempConfig.addMethodConfig(methodConfig, true);
                methodConfigs = tempConfig.getExcludingMethodConfigs();
                break;
            default:
                throw new RuntimeException("Not known tree type");
        }
        tree.addNode(methodConfig, methodConfigs);
    }


    @NotNull
    public static List<ValidationInfo> validateParameters(JPanel tablePanel, List<MethodConfig.Parameter> parameters) {
        List<ValidationInfo> validationInfos = new LinkedList<>();
        int size = parameters.size();
        for (int i = 0; i < size; i++) {
            MethodConfig.Parameter parameter = parameters.get(i);
            if (!MethodFormManager.isValidField(parameter.getType())) {
                validationInfos.add(new ValidationInfo("Parameter name must not contain spaces", tablePanel));
            }
            if (Objects.equals(parameter.getType(), "*") &&
                    i != parameters.size() - 1) {
                validationInfos.add(new ValidationInfo("Any method sign \"*\" have to be on last position", tablePanel));
            }
        }
        return validationInfos;
    }
}
