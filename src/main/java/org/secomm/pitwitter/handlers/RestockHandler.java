package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.discord.DiscordNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RestockHandler extends AbstractTwitterHandler {

    private static final Logger log = LoggerFactory.getLogger(RestockHandler.class);

    private static final String DEV_WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    private static final int PAGE_SIZE = 25;

    private String restockWebhook;

    private String giveawayWebhook;

    private SimpleDateFormat dateFormat;

    public RestockHandler(final RateLimiter rateLimiter,
                          final DatabaseHandler databaseHandler) {
        super(databaseHandler, rateLimiter);
        dateFormat = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
    }

    public void initialize() throws Exception {

//        restockWebhook = DEV_WEBHOOK;
//        giveawayWebhook = DEV_WEBHOOK;
        restockWebhook = databaseHandler.getWebhook(DatabaseHandler.DatabaseSelector.GLOBAL);
        giveawayWebhook = databaseHandler.getWebhook(DatabaseHandler.DatabaseSelector.GIVEAWAY);
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
                    DatabaseHandler.DatabaseSelector.RESTOCKS);
            if (status.getId() > lastId) {
                databaseHandler.updateLastId(status.getUser().getScreenName(), status.getId(),
                        DatabaseHandler.DatabaseSelector.RESTOCKS);
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            if (tweet.toUpperCase().contains("RESTOCK")) {
                sendNotification(restockWebhook, status);
            } else {
                for (String term : databaseHandler.getTerms(DatabaseHandler.DatabaseSelector.RESTOCKS)) {
                    if (!notificationSent && tweet.toUpperCase().contains(term.toUpperCase())) {
                        sendNotification(giveawayWebhook, status);
                        notificationSent = true;
                    }
                }
            }
        }
    }

    @Override
    public void run() {

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean run = true;

        while (run) {
            try {
                if (rateLimiter.twitterReady()) {
                    for (UserContext userContext : databaseHandler.getUsers(DatabaseHandler.DatabaseSelector.RESTOCKS)) {
                        rateLimiter.getUserTimeline(userContext, this);
                    }
                }
                lock.lock();
                try {
                    condition.await(1, TimeUnit.MINUTES);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            } catch (Exception e) {
                log.error("{} caught in restock handler run: {}", e.getClass().getName(), e.getLocalizedMessage());
            }
        }
    }
}
