package org.secomm.pitwitter.module;

import org.secomm.pitwitter.model.UserContext;
import org.secomm.pitwitter.database.RestocksDatabaseHandler;
import org.springframework.stereotype.Component;
import twitter4j.Status;

import java.util.List;

@Component
public class RestocksModule extends AbstractTwitterModule {

    private static final String DEV_WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    private final RestocksDatabaseHandler restocksDatabaseHandler;

    private String restocksWebhook;

    private String giveawaysWebhook;

    public RestocksModule(final RateLimiter rateLimiter,
                          final RestocksDatabaseHandler restocksDatabaseHandler) {
        super(rateLimiter);
        this.restocksDatabaseHandler = restocksDatabaseHandler;
    }

    public void initialize() throws Exception {

        restocksDatabaseHandler.initialize();
//        restocksWebhook = DEV_WEBHOOK;
//        giveawaysWebhook = DEV_WEBHOOK;
        restocksWebhook = restocksDatabaseHandler.getRestocksWebhook();
        giveawaysWebhook = restocksDatabaseHandler.getGiveawaysWebhook();
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
            long lastId = restocksDatabaseHandler.getLastId(userContext.getName());
            if (status.getId() > lastId) {
                restocksDatabaseHandler.updateLastId(userContext.getName(), status.getId());
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            if (tweet.toUpperCase().contains("RESTOCK")) {
                boolean excluded = false;
                List<String> exclusions = restocksDatabaseHandler.getExclusions();
                for (String exclude : exclusions) {
                    excluded |= tweet.toUpperCase().contains(exclude.toUpperCase());
                }
                if (!excluded) {
                    log.info("{} matched RESTOCK", status.getUser().getScreenName());
                    sendNotification(restocksWebhook, status);
                }
            } else {
                List<String> terms = restocksDatabaseHandler.getTerms();
                for (String term : terms) {
                    if (!notificationSent && tweet.toUpperCase().contains(term.toUpperCase())) {
                        log.info("{} matched {}", status.getUser().getScreenName(), term);
                        sendNotification(giveawaysWebhook, status);
                        notificationSent = true;
                    }
                }
            }
        }
    }

    @Override
    protected List<UserContext> getUsers() {
        return restocksDatabaseHandler.getUsers();
    }
}
