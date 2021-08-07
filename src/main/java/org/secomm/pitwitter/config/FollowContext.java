package org.secomm.pitwitter.config;

public class FollowContext {

    private String username;

    private long lastId;

    private String webhook;

    public FollowContext() {
    }

    public FollowContext(String username, long lastId, String webhook) {
        this.username = username;
        this.lastId = lastId;
        this.webhook = webhook;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getLastId() {
        return lastId;
    }

    public void setLastId(long lastId) {
        this.lastId = lastId;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    @Override
    public String toString() {
        return "FollowContext{" +
                "username='" + username + '\'' +
                ", lastId=" + lastId +
                ", webhook='" + webhook + '\'' +
                '}';
    }
}
