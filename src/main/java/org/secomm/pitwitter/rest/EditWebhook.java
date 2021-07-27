package org.secomm.pitwitter.rest;

public class EditWebhook {

    private String webhook;

    public EditWebhook() {
    }

    public EditWebhook(String webhook) {
        this.webhook = webhook;
    }

    public String getWebhook() {
        return webhook;
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    @Override
    public String toString() {
        return "EditWebhook{" +
                "webhook='" + webhook + '\'' +
                '}';
    }
}
