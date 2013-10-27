package com.brn;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.LangDataKeys;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.*;
import com.intellij.psi.codeStyle.JavaCodeStyleManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.ui.CollectionListModel;

/**
 * Created by IntelliJ IDEA.
 * User: bruno
 * Date: 10/27/13
 * Time: 11:57 AM
 */
public class GenerateAction extends AnAction {


    public void actionPerformed(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        GenerateDialog diag = new GenerateDialog(psiClass);
        diag.show();
        if (diag.isOK()) {
            generateCompareTo(psiClass, diag.getFields());
        }
    }

    private void generateCompareTo(final PsiClass psiClass, final CollectionListModel<PsiField> fields) {
        new WriteCommandAction.Simple(psiClass.getProject(), psiClass.getContainingFile()) {
            @Override
            protected void run() throws Throwable {
                StringBuilder constructorText = new StringBuilder();
                constructorText.append("private ");
                constructorText.append(psiClass.getName());
                constructorText.append("(Builder builder) {\n");
                for (PsiField field : fields.getItems()) {
                    constructorText.append("this.");
                    constructorText.append(field.getName());
                    constructorText.append(" = builder.");
                    constructorText.append(field.getName());
                    constructorText.append(";\n");
                }
                constructorText.append("}");

                PsiElementFactory elementFactory = JavaPsiFacade.getElementFactory(psiClass.getProject());
                PsiMethod constructorMethod = elementFactory.createMethodFromText(constructorText.toString(), psiClass);
                PsiElement constructorElement = psiClass.add(constructorMethod);
                JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(constructorElement);

                PsiClass builderClass = elementFactory.createClass("Builder");
                builderClass.getScope();
                for (PsiField psiField : fields.getItems()) {
                    PsiField field = elementFactory.createField(psiField.getName(), psiField.getType());
                    builderClass.add(field);
                }

                for (PsiField psiField : fields.getItems()) {
                    StringBuilder setterMethodBuilder = new StringBuilder();
                    setterMethodBuilder.append("public Builder ");
                    setterMethodBuilder.append(psiField.getName());
                    setterMethodBuilder.append("(");
                    setterMethodBuilder.append(psiField.getType().getPresentableText());
                    setterMethodBuilder.append(" ");
                    setterMethodBuilder.append(psiField.getName());
                    setterMethodBuilder.append(") {\n");
                    setterMethodBuilder.append("this.");
                    setterMethodBuilder.append(psiField.getName());
                    setterMethodBuilder.append(" = ");
                    setterMethodBuilder.append(psiField.getName());
                    setterMethodBuilder.append(";\n");
                    setterMethodBuilder.append("return this;\n");
                    setterMethodBuilder.append("}");
                    PsiMethod setterMethod = elementFactory.createMethodFromText(setterMethodBuilder.toString(), psiClass);
                    builderClass.add(setterMethod);

                }
                StringBuilder buildMethodText = new StringBuilder();
                buildMethodText.append("public ");
                buildMethodText.append(psiClass.getName());
                buildMethodText.append(" build() {\n");
                buildMethodText.append("return new ");
                buildMethodText.append(psiClass.getName());
                buildMethodText.append("(this);\n");
                buildMethodText.append("}\n");
                PsiMethod buildMethod = elementFactory.createMethodFromText(buildMethodText.toString(), psiClass);
                builderClass.add(buildMethod);

                PsiElement builderClassElement = psiClass.add(builderClass);
                JavaCodeStyleManager.getInstance(psiClass.getProject()).shortenClassReferences(builderClassElement);
            }
        }.execute();
    }

    @Override
    public void update(AnActionEvent e) {
        PsiClass psiClass = getPsiClassFromContext(e);
        e.getPresentation().setEnabled(psiClass != null);
    }

    private PsiClass getPsiClassFromContext(AnActionEvent e) {
        PsiFile psiFile = e.getData(LangDataKeys.PSI_FILE);
        Editor editor = e.getData(PlatformDataKeys.EDITOR);
        if (psiFile == null || editor == null) {
            e.getPresentation().setEnabled(false);
            return null;
        }
        int offset = editor.getCaretModel().getOffset();
        PsiElement elementAt = psiFile.findElementAt(offset);
        return PsiTreeUtil.getParentOfType(elementAt, PsiClass.class);
    }
}
