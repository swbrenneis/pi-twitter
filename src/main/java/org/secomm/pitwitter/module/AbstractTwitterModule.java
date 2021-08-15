package org.secomm.pitwitter.module;

import org.secomm.pitwitter.model.UserContext;
import org.secomm.pitwitter.discord.DiscordNotifier;
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

        rateLimiter.sendDiscordNotification(webhook, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }

    @Override
    public void run() {

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean run = true;

        while (run) {
            try {
                List<UserContext> userList = getUsers();
                if (rateLimiter.twitterReady()) {
                    for (UserContext userContext : userList) {
                        rateLimiter.queueTimelineRequest(userContext, this);
                    }
                }
                lock.lock();
                try {
                    run = !condition.await(1, TimeUnit.MINUTES);
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("{} caught while queueing user timelines: {}", e.getClass().getSimpleName(),
                        e.getLocalizedMessage());
            }
        }
    }
}
