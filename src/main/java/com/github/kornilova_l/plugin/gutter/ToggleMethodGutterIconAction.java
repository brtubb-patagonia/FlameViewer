package com.github.kornilova_l.plugin.gutter;

import com.github.kornilova_l.plugin.ProjectConfigManager;
import com.github.kornilova_l.plugin.config.ConfigStorage;
import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.debugger.impl.DebuggerUtilsEx;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.MarkupModelEx;
import com.intellij.openapi.editor.impl.DocumentMarkupModel;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiMethod;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.xdebugger.ui.DebuggerColors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToggleMethodGutterIconAction extends AnAction {
    @Override
    public void actionPerformed(AnActionEvent event) {
        final Project project = event.getData(CommonDataKeys.PROJECT);
        if (project == null) {
            return;
        }
        final Editor editor = getEditor(event, project);
        if (editor == null) {
            return;
        }
        PsiMethod method = getMethod(event, editor, project);
        if (method == null) {
            return;
        }
        assert event.getProject() != null;
        ConfigStorage.Config config = ProjectConfigManager.getConfig(event.getProject());
        if (!config.maybeRemove(method)) {
            config.addMethod(method);
        }
        DaemonCodeAnalyzer.getInstance(project).restart(event.getData(CommonDataKeys.PSI_FILE));
//        TextEditorHighlightingPass lineMarkersPass = (ServiceManager.getService(project, LineMarkersPassFactory.class)
//                .createHighlightingPass(
//                        event.getData(CommonDataKeys.PSI_FILE),
//                        editor
//                ));
//        if (lineMarkersPass instanceof LineMarkersPass) {
//            lineMarkersPass.doCollectInformation(new EmptyProgressIndicator());
//            lineMarkersPass.doApplyInformationToEditor();
//        }
//        System.out.println(lineMarkersPass.getClass());
    }

    private static void setIcon(PsiMethod method, Project project, Document document) {
        MarkupModelEx markupModel = (MarkupModelEx) DocumentMarkupModel.forDocument(document, project, true);
        RangeHighlighter highlighter = markupModel.addRangeHighlighter(
                method.getTextOffset(),
                method.getTextOffset() + 1,
                DebuggerColors.BREAKPOINT_HIGHLIGHTER_LAYER,
                null,
                HighlighterTargetArea.EXACT_RANGE);
        highlighter.setGutterIconRenderer(new ProfilerGutterIconRenderer());
    }

    @Nullable
    private static Editor getEditor(@NotNull AnActionEvent event, @NotNull Project project) {
        Editor editor = event.getData(CommonDataKeys.EDITOR);
        if (editor == null) {
            return FileEditorManager.getInstance(project).getSelectedTextEditor();
        }
        return editor;
    }

    @Nullable
    private static PsiMethod getMethod(@NotNull AnActionEvent event,
                                       @NotNull Editor editor,
                                       @NotNull Project project) {
        PsiMethod method = null;

        if (ActionPlaces.PROJECT_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.STRUCTURE_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.FAVORITES_VIEW_POPUP.equals(event.getPlace()) ||
                ActionPlaces.NAVIGATION_BAR_POPUP.equals(event.getPlace())) {
            final PsiElement psiElement = event.getData(CommonDataKeys.PSI_ELEMENT);
            if (psiElement instanceof PsiMethod) {
                final PsiFile containingFile = psiElement.getContainingFile();
                if (containingFile != null) {
                    method = (PsiMethod) psiElement;
                }
            }
        } else {
            Document document = editor.getDocument();
            PsiFile file = PsiDocumentManager.getInstance(project).getPsiFile(document);
            if (file != null) {
                final VirtualFile virtualFile = file.getVirtualFile();
                FileType fileType = virtualFile != null ? virtualFile.getFileType() : null;
                if (StdFileTypes.JAVA == fileType || StdFileTypes.CLASS == fileType) {
                    method = findMethod(project, editor);
                }
            }
        }
        return method;
    }

    @Nullable
    private static PsiMethod findMethod(Project project, Editor editor) {
        if (editor == null) {
            return null;
        }
        PsiFile psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.getDocument());
        if (psiFile == null) {
            return null;
        }
        final int offset = CharArrayUtil.shiftForward(editor.getDocument().getCharsSequence(), editor.getCaretModel().getOffset(), " \t");
        return DebuggerUtilsEx.findPsiMethod(psiFile, offset);
    }
}