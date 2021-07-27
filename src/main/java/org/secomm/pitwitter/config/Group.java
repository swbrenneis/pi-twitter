package org.secomm.pitwitter.config;

import java.util.List;

public class Group {

    private String name;

    private List<UserContext> userContexts;

    private List<String> terms;

    public Group(String name, List<UserContext> userContexts, List<String> terms) {
        this.name = name;
        this.userContexts = userContexts;
        this.terms = terms;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<UserContext> getUsers() {
        return userContexts;
    }

    public void setUsers(List<UserContext> userContexts) {
        this.userContexts = userContexts;
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
                ", users=" + userContexts +
                ", terms=" + terms +
                '}';
    }
}
