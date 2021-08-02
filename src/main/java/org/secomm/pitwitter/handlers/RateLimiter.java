package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.discord.DiscordNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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

    private final TwitterManager twitterManager;

    private final DiscordNotifier discordNotifier;

    private final Deque<Request> requestQueue;

    private final Deque<WebHook> webhookQueue;

    private final Lock queueLock;

    public RateLimiter(final TwitterManager twitterManager,
                       final DiscordNotifier discordNotifier) {

        this.twitterManager = twitterManager;
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
        SimpleDateFormat format = new SimpleDateFormat(TwitterManager.DATE_FORMAT);
        Date lastSearched = null;
        String lastSearchedString = userContext.getLastSearched();
        if (lastSearchedString != null) {
            lastSearched = format.parse(lastSearchedString);
        } else {
            lastSearched = new Date();
        }
        List<Status> statusList = twitterManager.getUserTimeline(userContext.getName(), PAGE_SIZE);

        return statusList;
    }

    @Override
    public void run() {

        Lock lock = new ReentrantLock();
        Condition condition = lock.newCondition();
        boolean run = true;
        List<Request> bucket = new ArrayList<>();

        while (run) {

            bucket.clear();
            queueLock.lock();
            try {
                while (bucket.size() < BUCKET_SIZE && !requestQueue.isEmpty()) {
                    bucket.add(requestQueue.pop());
                }
            } finally {
                queueLock.unlock();
            }

            for (Request request : bucket) {
                try {
                    List<Status> statuses = getUserTimeline(request.userContext);
                    request.handler.receivedStatuses(statuses);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            long now = System.currentTimeMillis() / 1000;
            while (now > discordNotifier.getSendAfter() && !webhookQueue.isEmpty()) {
                WebHook webHook = webhookQueue.peek();
                if (discordNotifier.sendWebhook(webHook.webhook, webHook.url)) {
                    webhookQueue.pop();
                }
            }

            lock.lock();
            try {
                run = !condition.await(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                lock.unlock();
            }
        }
    }
}
