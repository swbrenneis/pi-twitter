package org.secomm.pitwitter.module;

import org.secomm.pitwitter.model.UserContext;
import org.secomm.pitwitter.discord.DiscordAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import twitter4j.Status;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractTwitterModule implements TwitterModule {

    protected final Logger log;

    protected final RateLimiter rateLimiter;

    protected AbstractTwitterModule(final RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
        log = LoggerFactory.getLogger(getClass().getName());
    }

    protected abstract List<UserContext> getUsers();

    protected void sendNotification(String webhook, Status status) {

        rateLimiter.sendDiscordNotification(webhook, String.format(DiscordAdapter.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }

    @Override
    public void ready() {

        List<UserContext> userList = getUsers();
        for (UserContext userContext : userList) {
            rateLimiter.queueTimelineRequest(userContext, this);
        }
    }
}
