package org.secomm.pitwitter.webhook;

import java.util.List;

public class WebhookContent {

    private String username;

    private String avatar_url;

    private String content;

    private List<Embed> embeds;

    public WebhookContent(String username, String avatar_url, String content, List<Embed> embeds) {
        this.username = username;
        this.avatar_url = avatar_url;
        this.content = content;
        this.embeds = embeds;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAvatar_url() {
        return avatar_url;
    }

    public void setAvatar_url(String avatar_url) {
        this.avatar_url = avatar_url;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public List<Embed> getEmbeds() {
        return embeds;
    }

    public void setEmbeds(List<Embed> embeds) {
        this.embeds = embeds;
    }

    @Override
    public String toString() {
        return "WebhookContent{" +
                "username='" + username + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", content='" + content + '\'' +
                ", embeds=" + embeds +
                '}';
    }
}
