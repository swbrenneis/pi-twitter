package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.discord.DiscordNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Paging;
import twitter4j.Status;

import java.text.SimpleDateFormat;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class RateLimiter implements Runnable {

    private static final class Request{
        public UserContext userContext;
        public TwitterHandler handler;
        public Request(UserContext userContext, TwitterHandler handler) {
            this.userContext = userContext;
            this.handler = handler;
        }
    }

    private static final class WebHook {
        public String webhook;
        public String url;
        public WebHook(String webhook, String url) {
            this.webhook = webhook;
            this.url = url;
        }
    }

    private static final Logger log = LoggerFactory.getLogger(RateLimiter.class);

    private static final int BUCKET_SIZE = 12;

    private static final int PAGE_SIZE = 25;

    private final TwitterConnector twitterConnector;

    private final DiscordNotifier discordNotifier;

    private final Deque<Request> requestQueue;

    private final Deque<WebHook> webhookQueue;

    private final Lock queueLock;

    public RateLimiter(final TwitterConnector twitterConnector,
                       final DiscordNotifier discordNotifier) {

        this.twitterConnector = twitterConnector;
        this.discordNotifier = discordNotifier;
        requestQueue = new ArrayDeque<>();
        webhookQueue = new ArrayDeque<>();
        queueLock = new ReentrantLock();
    }

    public void getUserTimeline(UserContext userContext, TwitterHandler handler) {

        queueLock.lock();
        try {
            requestQueue.addLast(new Request(userContext, handler));
        } finally {
            queueLock.unlock();
        }
    }

    public void sendDiscordNotification(String webhook, String url) {

        queueLock.lock();
        try {
            webhookQueue.addLast(new WebHook(webhook, url));
        } finally {
            queueLock.unlock();
        }
    }

    private List<Status> getUserTimeline(UserContext userContext) throws Exception {

        log.info("Getting {} statuses for user {}", PAGE_SIZE, userContext.getName());
        List<Status> statusList = twitterConnector.getUserTimeline(userContext.getName(), PAGE_SIZE, userContext.getLastId());

        return statusList;
    }

    public boolean twitterReady() {
        return requestQueue.isEmpty();
    }

    @Override
    public void run() {

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean run = true;
        Deque<Request> bucket = new ArrayDeque<>();

        while (run) {
            queueLock.lock();
            bucket.clear();
            while (!requestQueue.isEmpty() && bucket.size() < 45) {
                bucket.addLast(requestQueue.pop());
            }
            try {
                log.info("Timeline request queue depth is {}", requestQueue.size());
                while (!bucket.isEmpty()) {
                    try {
                        Request request = bucket.pop();
                        List<Status> statuses = getUserTimeline(request.userContext);
                        request.handler.receivedStatuses(statuses);
                        Thread.sleep(1500);
                    } catch (Exception e) {
                        log.warn("{} caught while retrieving timelines: {}", e.getClass().getName(),
                                e.getLocalizedMessage());
                    }
                }
            } finally {
                queueLock.unlock();
            }

            long now = System.currentTimeMillis() / 1000;
            try {
                log.info("Discord notifications queue depth is {}", webhookQueue.size());
                while (now > discordNotifier.getSendAfter() && !webhookQueue.isEmpty()) {
                    WebHook webHook = webhookQueue.peek();
                    if (discordNotifier.sendWebhook(webHook.webhook, webHook.url)) {
                        webhookQueue.pop();
                    }
                    Thread.sleep(2000);
                }
            } catch (Exception e) {
                log.warn("{} caught while sending webhooks: {}", e.getClass().getName(),
                        e.getLocalizedMessage());
            }

            // Wait for more messages
            if (requestQueue.isEmpty()) {
                lock.lock();
                try {
                    run = !condition.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    }
}
