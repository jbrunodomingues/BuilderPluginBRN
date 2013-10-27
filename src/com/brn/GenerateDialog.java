package com.brn;

import com.intellij.ide.util.DefaultPsiElementCellRenderer;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.LabeledComponent;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.ui.CollectionListModel;
import com.intellij.ui.ToolbarDecorator;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

/**
 * Created by IntelliJ IDEA.
 * User: bruno
 * Date: 10/27/13
 * Time: 1:41 PM
 */
public class GenerateDialog extends DialogWrapper {

    private CollectionListModel<PsiField> myFields;
    private final LabeledComponent<JPanel> myComponent;



    public GenerateDialog(PsiClass psiClass) {
        super(psiClass.getProject());
        setTitle("Select Fields For Builder");
        myFields = new CollectionListModel<PsiField>(psiClass.getAllFields());
        JList fieldList = new JList(myFields);
        fieldList.setCellRenderer(new DefaultPsiElementCellRenderer());
        ToolbarDecorator decorator = ToolbarDecorator.createDecorator(fieldList);
        decorator.disableAddAction();
        JPanel panel = decorator.createPanel();
        myComponent = LabeledComponent.create(panel, "Fields to include in builder");
        init();
    }

    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return myComponent;
    }

    public CollectionListModel<PsiField> getFields() {
        return myFields;
    }


}
