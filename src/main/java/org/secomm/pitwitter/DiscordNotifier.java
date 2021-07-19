package org.secomm.pitwitter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.webhook.Embed;
import org.secomm.pitwitter.webhook.WebhookContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Component
@Scope("prototype")
@PropertySource("classpath:twitter4j.properties")
public class DiscordNotifier {

    private static final Logger log = LoggerFactory.getLogger(DiscordNotifier.class);

    @Value("${discord.webhook.terms}")
    private String termsWebHook;

    @Value("${discord.webhook.track}")
    private String trackWebHook;

    private final Gson gson;

    private final WebhookContent webhookContent;

    public DiscordNotifier() {

        gson = new GsonBuilder().create();
        webhookContent = new WebhookContent("",
                "",
                "",
                new ArrayList<>());
    }

    public void sendWebhook(String webHookName, twitter4j.User user, String content, List<Embed> embeds) {

        String webhook = "";
        switch (webHookName) {
            case "track":
                webhook = trackWebHook;
                break;
            case "term":
                webhook = termsWebHook;
                break;
        }

        try {
            webhookContent.setUsername(user.getName());
            webhookContent.setContent(String.format("@%s\n%s",user.getScreenName(), content));
            webhookContent.setAvatar_url(user.getBiggerProfileImageURLHttps());
            webhookContent.setEmbeds(embeds);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI(webhook))
                    .POST(HttpRequest.BodyPublishers.ofString(gson.toJson(webhookContent)))
                    .header("Content-Type", "application/json")
                    .build();

            HttpClient client = HttpClient.newBuilder().build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            log.info("Discord webhook notification response: {}", response.body());
        } catch (Exception e) {
            log.error("Exception in discord webhook notifier: {}", e.getLocalizedMessage());
        }
    }

}
