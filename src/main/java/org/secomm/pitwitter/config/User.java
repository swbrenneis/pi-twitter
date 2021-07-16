package org.secomm.pitwitter.config;

public class User {

    private String name;

    private String lastSearched;

    public User(String name, String lastSearched) {
        this.name = name;
        this.lastSearched = lastSearched;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastSearched() {
        return lastSearched;
    }

    public void setLastSearched(String lastSearched) {
        this.lastSearched = lastSearched;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", lastSearched='" + lastSearched + '\'' +
                '}';
    }
}
