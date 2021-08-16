package org.secomm.pitwitter.discord;

public class InvalidWebhookException extends Exception {
    public InvalidWebhookException() {
    }

    public InvalidWebhookException(String message) {
        super(message);
    }

    public InvalidWebhookException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidWebhookException(Throwable cause) {
        super(cause);
    }

    public InvalidWebhookException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
