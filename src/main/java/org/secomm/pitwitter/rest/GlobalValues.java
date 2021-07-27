package org.secomm.pitwitter.rest;

import java.util.List;

public class GlobalValues {

    private List<String> users;

    private List<String> terms;

    private String webhook;

    public GlobalValues() {
    }

    public GlobalValues(List<String> users, List<String> terms, String webhook) {
        this.users = users;
        this.terms = terms;
        this.webhook = webhook;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    @Override
    public String toString() {
        return "GlobalValues{" +
                "users=" + users +
                ", terms=" + terms +
                ", webhook='" + webhook + '\'' +
                '}';
    }
}
