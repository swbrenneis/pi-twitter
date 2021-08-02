package org.secomm.pitwitter.discord.webhook;

public class Thumbnail {

    private String url;

    private String proxy_url;

    private int height;

    private int width;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getProxy_url() {
        return proxy_url;
    }

    public void setProxy_url(String proxy_url) {
        this.proxy_url = proxy_url;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    @Override
    public String toString() {
        return "Thumbnail{" +
                "url='" + url + '\'' +
                ", proxy_url='" + proxy_url + '\'' +
                ", height=" + height +
                ", width=" + width +
                '}';
    }
}
