package org.secomm.pitwitter.webhook;

public class Thumbnail {

    private String url;

    public Thumbnail() {
    }

    public Thumbnail(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public String toString() {
        return "Thumbnail{" +
                "url='" + url + '\'' +
                '}';
    }
}
