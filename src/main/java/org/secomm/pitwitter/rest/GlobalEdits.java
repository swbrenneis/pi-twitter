package org.secomm.pitwitter.rest;

import java.util.List;

public class GlobalEdits {

    private String webHook;

    private List<String> addUsers;

    private List<String> deleteUsers;

    private List<String> addTerms;

    private List<String> deleteTerms;

    public GlobalEdits() {
    }

    public GlobalEdits(String webHook, List<String> addUsers, List<String> deleteUsers, List<String> addTerms,
                       List<String> deleteTerms) {
        this.webHook = webHook;
        this.addUsers = addUsers;
        this.deleteUsers = deleteUsers;
        this.addTerms = addTerms;
        this.deleteTerms = deleteTerms;
    }

    public String getWebHook() {
        return webHook;
    }

    public void setWebHook(String webHook) {
        this.webHook = webHook;
    }

    public List<String> getAddUsers() {
        return addUsers;
    }

    public void setAddUsers(List<String> addUsers) {
        this.addUsers = addUsers;
    }

    public List<String> getDeleteUsers() {
        return deleteUsers;
    }

    public void setDeleteUsers(List<String> deleteUsers) {
        this.deleteUsers = deleteUsers;
    }

    public List<String> getAddTerms() {
        return addTerms;
    }

    public void setAddTerms(List<String> addTerms) {
        this.addTerms = addTerms;
    }

    public List<String> getDeleteTerms() {
        return deleteTerms;
    }

    public void setDeleteTerms(List<String> deleteTerms) {
        this.deleteTerms = deleteTerms;
    }

    @Override
    public String toString() {
        return "GlobalEdits{" +
                "addUsers=" + addUsers +
                ", deleteUsers=" + deleteUsers +
                ", addTerms=" + addTerms +
                ", deleteTerms=" + deleteTerms +
                '}';
    }
}
