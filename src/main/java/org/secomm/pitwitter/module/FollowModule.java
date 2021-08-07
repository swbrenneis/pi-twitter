package org.secomm.pitwitter.module;

import org.secomm.pitwitter.config.FollowContext;
import org.secomm.pitwitter.handlers.DatabaseHandler;
import org.secomm.pitwitter.handlers.TwitterConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class FollowModule extends AbstractTwitterModule {

    private static final Logger log = LoggerFactory.getLogger(FollowModule.class);

    private String moduleName;

    private List<FollowContext> following;

    public FollowModule(DatabaseHandler databaseHandler, RateLimiter rateLimiter) {
        super(databaseHandler, rateLimiter);
    }

    public void initialize(String moduleName) {
        this.moduleName = moduleName;
    }

    @Override
    public void receivedStatuses(List<Status> statuses) {

        boolean firstpass = true;
        for (Status status : statuses) {
            if (firstpass) {
                String screenName = statuses.get(0).getUser().getScreenName();
                log.info("{} statuses received for {}", statuses.size(), screenName);
                firstpass = false;
            }
            long lastId = databaseHandler.getLastId(status.getUser().getScreenName(), moduleName);
            if (status.getId() > lastId) {
                databaseHandler.updateLastId(status.getUser().getScreenName(), status.getId(), moduleName);
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            for (String term : databaseHandler.getTerms(DatabaseHandler.DatabaseSelector.GLOBAL)) {
                if (!notificationSent && tweet.toUpperCase().contains(term.toUpperCase())) {
                    log.info("{} matched {}", status.getUser().getScreenName(), term);
                    String webhook = databaseHandler.getWebhook(status.getUser().getScreenName(), moduleName);
                    sendNotification(webhook, status);
                    notificationSent = true;
                }
            }
        }
    }

    @Override
    public void run() {

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean run = true;
        SimpleDateFormat dateFormat = new SimpleDateFormat(TwitterConnector.DATE_FORMAT);

        while (run) {
            try {
                if (rateLimiter.twitterReady()) {
                    for (FollowContext followContext : databaseHandler.getFollowing(moduleName)) {
                        rateLimiter.getUserTimeline(followContext, this);
                    }
                }
                lock.lock();
                try {
                    run = !condition.await(1, TimeUnit.MINUTES);
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("Configuration error: {}", e.getLocalizedMessage());
            }
        }
    }

    private String getWebhook(String username) {
        for (FollowContext followContext : databaseHandler.getFollowing(moduleName)) {
            if (followContext.getUsername().equals(username)) {
                return followContext.getWebhook();
            }
        }
        return null;
    }

}
