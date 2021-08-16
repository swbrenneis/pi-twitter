package org.secomm.pitwitter.discord;

import club.minnced.discord.webhook.WebhookClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:discord.properties")
public class DiscordAdapter {

    private static final Logger log = LoggerFactory.getLogger(DiscordAdapter.class);

    @Value("${token}")
    private String token;

    @Value("${listening}")
    private String listening;

    public static final String TWEET_URL_FORMAT = "https://twitter.com/%s/status/%s";

    public DiscordAdapter() {
    }

    public boolean sendWebhook(String webhookUrl, String url) {

        try {
            WebhookClient client = WebhookClient.withUrl(webhookUrl);
            client.send(url).thenAccept(readonlyMessage -> log.debug("{} sent to {}", readonlyMessage.getContent(),
                                    webhookUrl));
            client.close();
        } catch (Exception e) {
            log.error("{} in discord webhook notifier: {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
        return true;
    }

}
