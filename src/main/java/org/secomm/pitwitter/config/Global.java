package org.secomm.pitwitter.config;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.List;

@Document(collection = "global", schemaVersion = "1.0")
public class Global {

    @Id
    private String id;

    private List<UserContext> users;

    private List<String> terms;

    private String webhook;

    public Global() {
        id = "000001";
    }

    public Global(String id, List<UserContext> users, List<String> terms, String webhook) {
        this.id = id;
        this.users = users;
        this.terms = terms;
        this.webhook = webhook;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<UserContext> getUsers() {
        return users;
    }

    public void setUsers(List<UserContext> userContexts) {
        this.users = userContexts;
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
        return "Global{" +
                "id='" + id + '\'' +
                ", users=" + users +
                ", terms=" + terms +
                ", webhook='" + webhook + '\'' +
                '}';
    }
}
