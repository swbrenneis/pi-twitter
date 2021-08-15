package org.secomm.pitwitter.module;

import org.secomm.pitwitter.model.UserContext;
import org.secomm.pitwitter.connectors.TwitterConnector;
import org.secomm.pitwitter.database.GlobalDatabaseHandler;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

import java.util.List;

@Component
public class MatchModule extends AbstractTwitterModule {

    private static final String DEV_WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    public enum Operation { ADD, DELETE }

    private final GlobalDatabaseHandler globalDatabaseHandler;

    private String webhook;

    private final TwitterConnector twitterConnector;

    public MatchModule(final GlobalDatabaseHandler globalDatabaseHandler,
                       final RateLimiter rateLimiter,
                       final TwitterConnector twitterConnector) {
        super(rateLimiter);
        this.globalDatabaseHandler = globalDatabaseHandler;
        this.twitterConnector = twitterConnector;
    }

    public void initialize() throws TwitterException {

        globalDatabaseHandler.initialize();
        webhook = globalDatabaseHandler.getWebhook();
//        webhook = DEV_WEBHOOK;
    }

    @Override
    public void receivedStatuses(List<Status> statuses, UserContext userContext) {

        boolean firstpass = true;
        for (Status status : statuses) {
            if (firstpass) {
                String screenName = statuses.get(0).getUser().getScreenName();
                log.info("{} statuses received for {}", statuses.size(), screenName);
                firstpass = false;
            }
            long lastId = globalDatabaseHandler.getLastId(userContext.getName());
            if (status.getId() > lastId) {
                globalDatabaseHandler.updateLastId(userContext.getName(), status.getId());
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            List<String> terms = globalDatabaseHandler.getTerms();
            for (String term : terms) {
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
                        globalDatabaseHandler.addUser(userName);
                        return "User added";
                    }
                } catch (TwitterException e) {
                    log.error("Twitter exception in editUsers: {}", e.getLocalizedMessage());
                }
                break;
            case DELETE:
                globalDatabaseHandler.deleteUser(userName);
                return "User deleted";
        }
        return "Invalid operation";
    }

    public String editTerms(List<String> terms, Operation operation) {

        for (String term : terms) {
            switch (operation) {
                case ADD:
                    globalDatabaseHandler.addTerm(term);
                    break;
                case DELETE:
                    globalDatabaseHandler.deleteTerm(term);
            }
        }
        return "Success";
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
        globalDatabaseHandler.setWebhook(webhook);
    }

    @Override
    protected List<UserContext> getUsers() {
        return globalDatabaseHandler.getUsers();
    }
}
