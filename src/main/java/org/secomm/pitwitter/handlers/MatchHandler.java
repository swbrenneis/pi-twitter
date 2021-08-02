package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.discord.DiscordNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class MatchHandler implements TwitterHandler {

    private static final Logger log = LoggerFactory.getLogger(MatchHandler.class);

    private static final String DEV_WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    public enum Operation { ADD, DELETE }

    private String webhook;

    private final DatabaseHandler databaseHandler;

    private final RateLimiter rateLimiter;

    private final TwitterManager twitterManager;

    public MatchHandler(final DatabaseHandler databaseHandler,
                        final RateLimiter rateLimiter,
                        final TwitterManager twitterManager) {
        this.databaseHandler = databaseHandler;
        this.rateLimiter = rateLimiter;
        this.twitterManager = twitterManager;
    }

    public void initialize() throws TwitterException {

//        webhook = databaseHandler.getGlobalWebhook();
        webhook = DEV_WEBHOOK;
    }

    @Override
    public void receivedStatuses(List<Status> statuses) {

        for (Status status : statuses) {
            String tweet = status.getText();
            for (String term : databaseHandler.getGlobalTerms()) {
                if (tweet.toUpperCase().contains(term.toUpperCase())) {
                    sendNotification(status);
                }
            }
        }
    }

    public String editUser(String userName, Operation operation) {

        switch (operation) {
            case ADD:
                try {
                    User user = twitterManager.showUser(userName);
                    if (user == null) {
                        log.warn("User {} does not exist", userName);
                        return "No such user";
                    } else if (user.isProtected()) {
                        log.warn("User {} timeline is private", userName);
                        return "User timeline is private";
                    } else {
                        databaseHandler.addUser(userName);
                        return "User added";
                    }
                } catch (TwitterException e) {
                    log.error("Twitter exception in editUsers: {}", e.getLocalizedMessage());
                }
                break;
            case DELETE:
                databaseHandler.deleteUser(userName);
                return "User deleted";
        }
        return "Invalid operation";
    }

    public String editTerms(List<String> terms, Operation operation) {

        for (String term : terms) {
            switch (operation) {
                case ADD:
                    databaseHandler.addTerm(term);
                    break;
                case DELETE:
                    databaseHandler.deleteTerm(term);
            }
        }
        return "Success";
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
        databaseHandler.setWebhook(webhook);
    }

    @Override
    public void run() {

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean run = true;
        SimpleDateFormat dateFormat = new SimpleDateFormat(TwitterManager.DATE_FORMAT);

        while (run) {
            try {
                for (UserContext userContext : databaseHandler.getGlobalUsers()) {
                    rateLimiter.getUserTimeline(userContext, this);
                    databaseHandler.updateGlobalSearchTime(userContext.getName(), dateFormat.format(new Date()));
                }
                lock.lock();
                try {
                    run = !condition.await(3, TimeUnit.MINUTES);
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("Configuration error: {}", e.getLocalizedMessage());
            }
        }
    }

    private void sendNotification(Status status) {

        rateLimiter.sendDiscordNotification(webhook, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }

    public Global getGlobal() {
        return databaseHandler.getGlobal();
    }
}
