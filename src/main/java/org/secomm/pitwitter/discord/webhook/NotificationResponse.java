package org.secomm.pitwitter.discord.webhook;

public class NotificationResponse {

    private String message;

    private float retry_after;

    private boolean global;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public float getRetry_after() {
        return retry_after;
    }

    public void setRetry_after(float retry_after) {
        this.retry_after = retry_after;
    }

    public boolean isGlobal() {
        return global;
    }

    public void setGlobal(boolean global) {
        this.global = global;
    }

    @Override
    public String toString() {
        return "ErrorResult{" +
                "message='" + message + '\'' +
                ", retry_after=" + retry_after +
                ", global=" + global +
                '}';
    }
}
