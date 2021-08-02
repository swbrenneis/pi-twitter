package org.secomm.pitwitter.discord.webhook;

public class TwitterWebhook extends WebhookContent {

    public TwitterWebhook(String url) {
        super();
//        this.content = String.format("{{%s}}", url);
        this.content = url;
    }
}
