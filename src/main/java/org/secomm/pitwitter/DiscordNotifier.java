package org.secomm.pitwitter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.webhook.Embed;
import org.secomm.pitwitter.webhook.Footer;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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

    public void sendWebhook(String webhook, twitter4j.User user, String content, String description, List<Embed> embeds) {

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");
        Date now = new Date();
        Embed timestampEmbed = new Embed();
//        Footer footer = new Footer();
        timestampEmbed.setDescription(description);
        timestampEmbed.setTimestamp(String.format("%sT%sZ", dateFormat.format(now), timeFormat.format(now)));
//        timestampEmbed.setFooter(footer);
//        timestampEmbed.setTimestamp(String.format("%sT%sZ", dateFormat.format(now), timeFormat.format(now)));
        embeds.add(timestampEmbed);
        try {
            webhookContent.setUsername(user.getName());
            webhookContent.setContent(String.format("@%s\n%s",user.getScreenName(), content));
            webhookContent.setAvatar_url(user.getBiggerProfileImageURLHttps());
            webhookContent.setEmbeds(embeds);
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
        } catch (Exception e) {
            log.error("Exception in discord webhook notifier: {}", e.getLocalizedMessage());
        }
    }
}
