package org.secomm.pitwitter.rest;

import java.util.List;

public class EditList {

    public enum EditAction { ADD, DELETE }

    private EditAction editAction;

    private List<String> edits;

    public EditList() {
    }

    public EditList(EditAction editAction, List<String> edits) {
        this.editAction = editAction;
        this.edits = edits;
    }

    public EditAction getEditAction() {
        return editAction;
    }

    public void setEditAction(EditAction editAction) {
        this.editAction = editAction;
    }

    public List<String> getEdits() {
        return edits;
    }

    public void setEdits(List<String> edits) {
        this.edits = edits;
    }

    @Override
    public String toString() {
        return "EditList{" +
                "editAction=" + editAction +
                ", edits=" + edits +
                '}';
    }
}
