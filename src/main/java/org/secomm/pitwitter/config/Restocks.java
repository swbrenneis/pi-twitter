package org.secomm.pitwitter.config;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.List;

@Document(collection = "restocks", schemaVersion = "1.0")
public class Restocks {

    @Id
    private String id;

    private List<UserContext> users;

    private List<String> terms;

    private List<String> excludes;

    private String restockWebhook;

    private String giveawayWebhook;

    public Restocks() {
        id = "000002";
    }

    public Restocks(String id, List<UserContext> users, List<String> terms, List<String> excludes,
                    String restockWebhook, String giveawayWebhook) {
        this.id = id;
        this.users = users;
        this.terms = terms;
        this.excludes = excludes;
        this.restockWebhook = restockWebhook;
        this.giveawayWebhook = giveawayWebhook;
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

    public List<String> getExcludes() {
        return excludes;
    }

    public void setExcludes(List<String> excludes) {
        this.excludes = excludes;
    }

    public String getRestockWebhook() {
        return restockWebhook;
    }

    public void setRestockWebhook(String restockWebhook) {
        this.restockWebhook = restockWebhook;
    }

    public String getGiveawayWebhook() {
        return giveawayWebhook;
    }

    public void setGiveawayWebhook(String giveawayWebhook) {
        this.giveawayWebhook = giveawayWebhook;
    }

    @Override
    public String toString() {
        return "Restocks{" +
                "id='" + id + '\'' +
                ", users=" + users +
                ", terms=" + terms +
                ", excludes=" + excludes +
                ", restockWebhook='" + restockWebhook + '\'' +
                ", giveawayWebhook='" + giveawayWebhook + '\'' +
                '}';
    }
}
