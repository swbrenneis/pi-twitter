package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.DiscordNotifier;
import org.secomm.pitwitter.webhook.Embed;
import org.secomm.pitwitter.webhook.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.*;

import java.util.ArrayList;
import java.util.List;

@Component
public class PiStatusListener implements StatusListener {

    private static final Logger log = LoggerFactory.getLogger(StatusListener.class);

    // Dev
    //private static final String WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    //Prod
    private static final String WEBHOOK = "https://discord.com/api/webhooks/866514841002967071/EkA9kDA80PvafbgvaS7VX5f35tR_EZpdFA7jhXZSXuWHnh_cEsk-RBoIPvrYkFQoUW9T";

    private final DiscordNotifier discordNotifier;

    public PiStatusListener(final DiscordNotifier discordNotifier) {
        this.discordNotifier = discordNotifier;
    }

    @Override
    public void onStatus(Status status) {

        log.info("Mention by {}", status.getUser().getScreenName());
        List<Embed> embeds = new ArrayList<>();
        MediaEntity[] mediaEntities = status.getMediaEntities();
        boolean imageAdded = false; // Only add the first photo if there is one.
        for (MediaEntity mediaEntity : mediaEntities) {
            if (mediaEntity.getType().equals("photo") && !imageAdded) {
                Embed imageEmbed = new Embed();
                String imageUrl = mediaEntity.getMediaURLHttps();
                int index = imageUrl.indexOf("https");
                imageUrl = imageUrl.substring(index);
                imageUrl = imageUrl.substring(0, imageUrl.length());
                Image image = new Image(imageUrl);
                imageEmbed.setImage(image);
                embeds.add(imageEmbed);
                log.info("Embedded image at {}", imageUrl);
                imageAdded = true;

            }
        }
        discordNotifier.sendWebhook(WEBHOOK, status.getUser(), status.getText(), "Twitter Mentions Monitor", embeds);
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
