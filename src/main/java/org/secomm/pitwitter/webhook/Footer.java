package org.secomm.pitwitter.webhook;

public class Footer {

    private String text;

    private String icon_url;

    public Footer() {
    }

    public Footer(String text, String icon_url) {
        this.text = text;
        this.icon_url = icon_url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    @Override
    public String toString() {
        return "Footer{" +
                "text='" + text + '\'' +
                ", icon_url='" + icon_url + '\'' +
                '}';
    }
}
