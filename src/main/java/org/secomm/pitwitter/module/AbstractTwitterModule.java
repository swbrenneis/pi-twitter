package org.secomm.pitwitter.module;

import org.secomm.pitwitter.discord.DiscordNotifier;
import org.secomm.pitwitter.database.GlobalDatabaseHandler;
import twitter4j.Status;

public abstract class AbstractTwitterModule implements TwitterModule {

    protected final RateLimiter rateLimiter;

    protected AbstractTwitterModule(final RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    protected void sendNotification(String webhook, Status status) {

        rateLimiter.sendDiscordNotification(webhook, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }

}
