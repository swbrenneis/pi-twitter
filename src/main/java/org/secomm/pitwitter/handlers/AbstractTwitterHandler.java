package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.discord.DiscordNotifier;
import twitter4j.Status;

public abstract class AbstractTwitterHandler implements TwitterHandler {

    protected final DatabaseHandler databaseHandler;

    protected final RateLimiter rateLimiter;

    protected AbstractTwitterHandler(final DatabaseHandler databaseHandler,
                                     final RateLimiter rateLimiter) {
        this.databaseHandler = databaseHandler;
        this.rateLimiter = rateLimiter;
    }

    protected void sendNotification(String webhook, Status status) {

        rateLimiter.sendDiscordNotification(webhook, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }

}
