package org.secomm.pitwitter.discord.webhook;

public class Footer {

    private String text;

    private String icon_url;

    private String proxy_icon_url;

    public Footer() {
    }

    public Footer(String text, String icon_url, String proxy_icon_url) {
        this.text = text;
        this.icon_url = icon_url;
        this.proxy_icon_url = proxy_icon_url;
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

    public String getProxy_icon_url() {
        return proxy_icon_url;
    }

    public void setProxy_icon_url(String proxy_icon_url) {
        this.proxy_icon_url = proxy_icon_url;
    }

    @Override
    public String toString() {
        return "Footer{" +
                "text='" + text + '\'' +
                ", icon_url='" + icon_url + '\'' +
                ", proxy_icon_url='" + proxy_icon_url + '\'' +
                '}';
    }
}
