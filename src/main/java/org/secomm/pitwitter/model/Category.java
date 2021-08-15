package org.secomm.pitwitter.model;

import java.util.List;

public class Category {

    private String category;

    private List<UserContext> users;

    private List<String> terms;

    private String webhook;

    public Category() {
    }

    public Category(String category, List<UserContext> users, List<String> terms, String webhook) {
        this.category = category;
        this.users = users;
        this.terms = terms;
        this.webhook = webhook;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<UserContext> getUsers() {
        return users;
    }

    public void setUsers(List<UserContext> users) {
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
        return "Category{" +
                "categoryName='" + category + '\'' +
                ", users=" + users +
                ", terms=" + terms +
                ", webhook='" + webhook + '\'' +
                '}';
    }
}
