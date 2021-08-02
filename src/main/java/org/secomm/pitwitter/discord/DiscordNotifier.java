package org.secomm.pitwitter.discord;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.discord.webhook.NotificationResponse;
import org.secomm.pitwitter.discord.webhook.TwitterWebhook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class DiscordNotifier {

    private static final Logger log = LoggerFactory.getLogger(DiscordNotifier.class);

    public static final String ISO8601_FORMAT = "yyyy-MM-dd'T'HH:mm'Z'";

    public static final String TWEET_URL_FORMAT = "https://twitter.com/%s/status/%s";

    private final Gson gson;

    private long sendAfter;

    public DiscordNotifier() {

        gson = new GsonBuilder().create();
        sendAfter = System.currentTimeMillis() / 1000;
    }

    public boolean sendWebhook(String webhook, String url) {

        TwitterWebhook webhookContent = new TwitterWebhook(url);
        try {
            String json = gson.toJson(webhookContent);
            log.info("Sending webhook: " + json);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(webhook))
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .header("Content-Type", "application/json")
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Discord webhook notification response: {}", response.body());
            if (response.statusCode() != 204) {
                NotificationResponse notificationResponse = gson.fromJson(response.body(), NotificationResponse.class);
                float retryAfter = notificationResponse.getRetry_after();
                sendAfter = (System.currentTimeMillis() / 1000) + (long) retryAfter + 10;
                return false;
            }
        } catch (Exception e) {
            log.error("Exception in discord webhook notifier: {}", e.getLocalizedMessage());
        }

        return true;
    }

    public long getSendAfter() {
        return sendAfter;
    }
}
