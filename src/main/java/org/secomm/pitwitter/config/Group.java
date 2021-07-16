package org.secomm.pitwitter.config;

import java.util.List;

public class Group {

    private String name;

    private List<User> users;

    private List<String> terms;

    public Group(String name, List<User> users, List<String> terms) {
        this.name = name;
        this.users = users;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
        return "Group{" +
                "name='" + name + '\'' +
                ", users=" + users +
                ", terms=" + terms +
                '}';
    }
}
