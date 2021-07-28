package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.DiscordNotifier;
import org.secomm.pitwitter.webhook.Embed;
import org.secomm.pitwitter.webhook.Image;
import org.springframework.stereotype.Component;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class PiStatusListener implements StatusListener {

    private final DiscordNotifier discordNotifier;

    public PiStatusListener(final DiscordNotifier discordNotifier) {
        this.discordNotifier = discordNotifier;
    }

    @Override
    public void onStatus(Status status) {

        List<Embed> embeds = new ArrayList<>();
        MediaEntity[] mediaEntities = status.getMediaEntities();
        boolean imageAdded = false; // Only add the first photo if there is one.
        for (MediaEntity mediaEntity : mediaEntities) {
            if (mediaEntity.getType().equals("photo") && !imageAdded) {
                Embed imageEmbed = new Embed();
                Image image = new Image(mediaEntity.getMediaURLHttps());
                imageEmbed.setImage(image);
                embeds.add(imageEmbed);
                imageAdded = true;
            }
        }
        discordNotifier.sendWebhook("track", status.getUser(), status.getText(), embeds);
    }

    @Override
    public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {

    }

    @Override
    public void onTrackLimitationNotice(int i) {

    }

    @Override
    public void onScrubGeo(long l, long l1) {

    }

    @Override
    public void onStallWarning(StallWarning stallWarning) {

    }

    @Override
    public void onException(Exception e) {

    }
}
