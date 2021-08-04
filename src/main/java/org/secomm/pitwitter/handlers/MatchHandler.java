package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.UserContext;
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
public class MatchHandler extends AbstractTwitterHandler {

    private static final Logger log = LoggerFactory.getLogger(MatchHandler.class);

    private static final String DEV_WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    public enum Operation { ADD, DELETE }

    private String webhook;

    private final SimpleDateFormat dateFormat;

    private final TwitterConnector twitterConnector;

    public MatchHandler(final DatabaseHandler databaseHandler,
                        final RateLimiter rateLimiter,
                        final TwitterConnector twitterConnector) {
        super(databaseHandler, rateLimiter);
        this.twitterConnector = twitterConnector;
        dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    }

    public void initialize() throws TwitterException {

        webhook = databaseHandler.getWebhook(DatabaseHandler.DatabaseSelector.GLOBAL);
//        webhook = DEV_WEBHOOK;
    }

    @Override
    public void receivedStatuses(List<Status> statuses) {

        boolean firstpass = true;
        for (Status status : statuses) {
            if (firstpass) {
                String screenName = statuses.get(0).getUser().getScreenName();
                String createdAt = dateFormat.format(statuses.get(0).getUser().getCreatedAt());
                log.info("{} statuses received for {}, first status date is {}", statuses.size(), screenName, createdAt);
                firstpass = false;
            }
            long lastId = databaseHandler.getLastId(status.getUser().getScreenName(),
                    DatabaseHandler.DatabaseSelector.GLOBAL);
            if (status.getId() > lastId) {
                databaseHandler.updateLastId(status.getUser().getScreenName(), status.getId(),
                        DatabaseHandler.DatabaseSelector.GLOBAL);
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            for (String term : databaseHandler.getTerms(DatabaseHandler.DatabaseSelector.GLOBAL)) {
                if (!notificationSent && tweet.toUpperCase().contains(term.toUpperCase())) {
                    log.info("{} matched {}", status.getUser().getScreenName(), term);
                    sendNotification(webhook, status);
                    notificationSent = true;
                }
            }
        }
    }

    public String editUser(String userName, Operation operation) {

        switch (operation) {
            case ADD:
                try {
                    User user = twitterConnector.showUser(userName);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(TwitterConnector.DATE_FORMAT);

        while (run) {
            try {
                if (rateLimiter.twitterReady()) {
                    for (UserContext userContext : databaseHandler.getUsers(DatabaseHandler.DatabaseSelector.GLOBAL)) {
                        rateLimiter.getUserTimeline(userContext, this);
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

    public Global getGlobal() {
        return databaseHandler.getGlobal();
    }
}
