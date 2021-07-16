package org.secomm.pitwitter.config;

import java.util.List;

public class Global {

    private List<User> users;

    private List<String> searches;

    public Global(List<User> users, List<String> searches) {
        this.users = users;
        this.searches = searches;
    }

    public List<User> getUsers() {
        return users;
    }

    public void setUsers(List<User> users) {
        this.users = users;
    }

    public List<String> getSearches() {
        return searches;
    }

    public void setSearches(List<String> searches) {
        this.searches = searches;
    }

    @Override
    public String toString() {
        return "Global{" +
                "users=" + users +
                ", searches=" + searches +
                '}';
    }
}
