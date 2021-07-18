package org.secomm.pitwitter.config;

import io.jsondb.annotation.Document;
import io.jsondb.annotation.Id;

import java.util.List;

@Document(collection = "global", schemaVersion = "1.0")
public class Global {

    @Id
    private String id;

    private List<User> users;

    private List<String> terms;

    public Global() {
        id = "000001";
    }

    public Global(String id, List<User> users, List<String> terms) {
        this.id = id;
        this.users = users;
        this.terms = terms;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<String> getTerms() {
        return terms;
    }

    public void setTerms(List<String> terms) {
        this.terms = terms;
    }

    @Override
    public String toString() {
        return "Global{" +
                "Id='" + id + '\'' +
                ", users=" + users +
                ", searches=" + terms +
                '}';
    }
}
