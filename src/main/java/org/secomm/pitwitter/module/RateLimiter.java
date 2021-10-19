package org.secomm.pitwitter.module;

import org.secomm.pitwitter.connectors.TwitterConnector;
import org.secomm.pitwitter.discord.DiscordAdapter;
import org.secomm.pitwitter.model.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.Status;

import java.util.ArrayDeque;
import java.util.ArrayList;
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
        public TwitterModule handler;
        public Request(UserContext userContext, TwitterModule handler) {
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

    private final DiscordAdapter discordAdapter;

    private final Deque<Request> requestQueue;

    private final Deque<WebHook> webhookQueue;

    private final List<TwitterModule> modules;

    private final Lock queueLock;

    public RateLimiter(final TwitterConnector twitterConnector,
                       final DiscordAdapter discordAdapter) {

        this.twitterConnector = twitterConnector;
        this.discordAdapter = discordAdapter;
        requestQueue = new ArrayDeque<>();
        webhookQueue = new ArrayDeque<>();
        queueLock = new ReentrantLock();
        modules = new ArrayList<>();
    }

    public void register(TwitterModule module) {
        modules.add(module);
    }

    public void queueTimelineRequest(UserContext userContext, TwitterModule handler) {

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

    private List<Status> queueTimelineRequest(UserContext userContext) throws Exception {

        log.debug("Getting {} statuses for user {}", PAGE_SIZE, userContext.getName());
        List<Status> statusList = twitterConnector.getUserTimeline(userContext.getName(), PAGE_SIZE, userContext.getLastId());

        return statusList;
    }

    @Override
    public void run() {

        Lock runLock = new ReentrantLock();
        Condition runCondition = runLock.newCondition();
        boolean runFlag = true;
        Deque<Request> bucket = new ArrayDeque<>();

        while (runFlag) {
            queueLock.lock();
            bucket.clear();
            log.info("Timeline request queue depth is {}", requestQueue.size());
            while (!requestQueue.isEmpty() && bucket.size() < 45) {
                bucket.addLast(requestQueue.pop());
            }
            try {
                while (!bucket.isEmpty()) {
                    try {
                        Request request = bucket.pop();
                        List<Status> statuses = queueTimelineRequest(request.userContext);
                        request.handler.receivedStatuses(statuses, request.userContext);
                        Thread.sleep(1500);
                    } catch (Exception e) {
                        log.warn("{} caught while retrieving timelines: {}", e.getClass().getName(),
                                e.getLocalizedMessage());
                    }
                }
            } finally {
                queueLock.unlock();
            }

            try {
                log.info("Discord notifications queue depth is {}", webhookQueue.size());
                while (!webhookQueue.isEmpty()) {
                    WebHook webHook = webhookQueue.pop();
                    discordAdapter.sendWebhook(webHook.webhook, webHook.url);
                }
            } catch (Exception e) {
                log.warn("{} caught while sending webhooks: {}", e.getClass().getName(),
                        e.getLocalizedMessage());
            }

            // Wait for more messages
            if (requestQueue.isEmpty()) {
                runLock.lock();
                try {
                    runFlag = !runCondition.await(10, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    runLock.unlock();
                }

                for (TwitterModule module : modules) {
                    module.ready();
                }
            }
        }
    }
}
