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
public class RestockHandler implements TwitterHandler {

    private static final Logger log = LoggerFactory.getLogger(RestockHandler.class);

    private static final String DEV_WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    private static final int PAGE_SIZE = 25;

    private final RateLimiter rateLimiter;

    private final DatabaseHandler databaseHandler;

    private String restockWebhook;

    private String giveawayWebhook;

    public RestockHandler(final RateLimiter rateLimiter,
                          final DatabaseHandler databaseHandler) {
        this.rateLimiter = rateLimiter;
        this.databaseHandler = databaseHandler;
    }

    public void initialize() throws Exception {

        restockWebhook = DEV_WEBHOOK;
        giveawayWebhook = DEV_WEBHOOK;
//        restockWebhook = databaseHandler.getRestocksWebhook();
//        giveawayWebhook = databaseHandler.getGiveawayWebhook();
    }

    @Override
    public void receivedStatuses(List<Status> statuses) {

        for (Status status : statuses) {
            String tweet = status.getText();
            if (tweet.toUpperCase().contains("RESTOCK")) {
                sendRestockNotification(status);
            } else {
                for (String term : databaseHandler.getRestocksTerms()) {
                    if (tweet.toUpperCase().contains(term.toUpperCase())) {
                        sendGiveawayNotification(status);
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
        SimpleDateFormat dateFormat = new SimpleDateFormat(TwitterManager.DATE_FORMAT);

        while (run) {
            try {
                for (UserContext userContext : databaseHandler.getRestocksUsers()) {
                    rateLimiter.getUserTimeline(userContext, this);
                    databaseHandler.updateRestocksSearchTime(userContext.getName(), dateFormat.format(new Date()));
                }
                lock.lock();
                try {
                    condition.await(3, TimeUnit.MINUTES);
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

    private void sendRestockNotification(Status status) {

        rateLimiter.sendDiscordNotification(restockWebhook, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }

    private void sendGiveawayNotification(Status status) {

        rateLimiter.sendDiscordNotification(giveawayWebhook, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
    }
}
