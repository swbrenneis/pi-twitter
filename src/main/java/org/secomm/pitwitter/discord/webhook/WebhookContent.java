package org.secomm.pitwitter.discord.webhook;

import java.util.List;

public class WebhookContent {

    protected String content;

    private String username;

    private String avatar_url;

    private boolean tts;

    private String file;

    private List<Embed> embeds;

    private String json_payload;

    private AllowedMention allowed_mentions;

    private List<MessageComponent> message_components;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public boolean isTts() {
        return tts;
    }

    public void setTts(boolean tts) {
        this.tts = tts;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public List<Embed> getEmbeds() {
        return embeds;
    }

    public void setEmbeds(List<Embed> embeds) {
        this.embeds = embeds;
    }

    public String getJson_payload() {
        return json_payload;
    }

    public void setJson_payload(String json_payload) {
        this.json_payload = json_payload;
    }

    public AllowedMention getAllowed_mentions() {
        return allowed_mentions;
    }

    public void setAllowed_mentions(AllowedMention allowed_mentions) {
        this.allowed_mentions = allowed_mentions;
    }

    public List<MessageComponent> getMessage_components() {
        return message_components;
    }

    public void setMessage_components(List<MessageComponent> message_components) {
        this.message_components = message_components;
    }

    @Override
    public String toString() {
        return "WebhookContent{" +
                "content='" + content + '\'' +
                ", username='" + username + '\'' +
                ", avatar_url='" + avatar_url + '\'' +
                ", tts=" + tts +
                ", file='" + file + '\'' +
                ", embeds=" + embeds +
                ", json_payload='" + json_payload + '\'' +
                ", allowed_mentions=" + allowed_mentions +
                ", message_components=" + message_components +
                '}';
    }
}
