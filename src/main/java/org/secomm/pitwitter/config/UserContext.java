package org.secomm.pitwitter.config;

public class UserContext {

    private String name;

    private long lastId;
    
    public UserContext(String name, long lastId) {
        this.name = name;
        this.lastId = lastId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", lastId='" + lastId + '\'' +
                '}';
    }
}
