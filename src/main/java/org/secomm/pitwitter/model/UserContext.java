package org.secomm.pitwitter.model;

public class UserContext {

    private String name;

    private long lastId;

    private String category;

    public UserContext(String name, long lastId) {
        this.name = name;
        this.lastId = lastId;
    }

    public UserContext(String name, long lastId, String category) {
        this.name = name;
        this.lastId = lastId;
        this.category = category;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "UserContext{" +
                "name='" + name + '\'' +
                ", lastId=" + lastId +
                ", category='" + category + '\'' +
                '}';
    }
}
