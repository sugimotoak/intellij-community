package com.intellij.openapi.editor.impl;

import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.editor.EditorBundle;
import com.intellij.openapi.editor.ReadOnlyFragmentModificationException;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.actionSystem.*;
import com.intellij.openapi.ui.Messages;
import com.intellij.injected.editor.DocumentWindow;
import org.jetbrains.annotations.NotNull;

public class EditorActionManagerImpl extends EditorActionManager {
  private final TypedAction myTypedAction = new TypedAction();
  private ReadonlyFragmentModificationHandler myReadonlyFragmentsHandler = new DefaultReadOnlyFragmentModificationHandler();
  private final ActionManager myActionManager;

  public EditorActionManagerImpl(ActionManager actionManager) {
    myActionManager = actionManager;
  }

  public EditorActionHandler getActionHandler(@NotNull String actionId) {
    return ((EditorAction) myActionManager.getAction(actionId)).getHandler();
  }

  public EditorActionHandler setActionHandler(@NotNull String actionId, @NotNull EditorActionHandler handler) {
    EditorAction action = (EditorAction)myActionManager.getAction(actionId);
    return action.setupHandler(handler);
  }

  @NotNull
  public TypedAction getTypedAction() {
    return myTypedAction;
  }

  public ReadonlyFragmentModificationHandler getReadonlyFragmentModificationHandler() {
    return myReadonlyFragmentsHandler;
  }

  @Override
  public ReadonlyFragmentModificationHandler getReadonlyFragmentModificationHandler(@NotNull final Document document) {
    final Document doc = document instanceof DocumentWindow ? ((DocumentWindow)document).getDelegate() : document;
    final ReadonlyFragmentModificationHandler docHandler =
      doc instanceof DocumentImpl ? ((DocumentImpl)doc).getReadonlyFragmentModificationHandler() : null;
    return docHandler == null ? myReadonlyFragmentsHandler : docHandler;
  }

  @Override
  public void setReadonlyFragmentModificationHandler(@NotNull final Document document, final ReadonlyFragmentModificationHandler handler) {
    final Document doc = document instanceof DocumentWindow ? ((DocumentWindow)document).getDelegate() : document;
    if (doc instanceof DocumentImpl) {
      ((DocumentImpl)document).setReadonlyFragmentModificationHandler(handler);
    }
  }

  public ReadonlyFragmentModificationHandler setReadonlyFragmentModificationHandler(@NotNull ReadonlyFragmentModificationHandler handler) {
    ReadonlyFragmentModificationHandler oldHandler = myReadonlyFragmentsHandler;
    myReadonlyFragmentsHandler = handler;
    return oldHandler;
  }


  private static class DefaultReadOnlyFragmentModificationHandler implements ReadonlyFragmentModificationHandler {
    public void handle(ReadOnlyFragmentModificationException e) {
      Messages.showErrorDialog(EditorBundle.message("guarded.block.modification.attempt.error.message"),
                               EditorBundle.message("guarded.block.modification.attempt.error.title"));
    }
  }
}

