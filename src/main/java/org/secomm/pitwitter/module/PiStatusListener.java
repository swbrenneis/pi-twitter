package org.secomm.pitwitter.module;

import org.secomm.pitwitter.discord.DiscordNotifier;
import org.secomm.pitwitter.module.RateLimiter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import twitter4j.*;

@Component
public class PiStatusListener implements StatusListener {

    private static final Logger log = LoggerFactory.getLogger(StatusListener.class);

    // Dev
    //private static final String WEBHOOK = "https://discordapp.com/api/webhooks/865325874077499433/J-2fsnn1gZCkYoebA7uq12ZFqvWixwIgfnKv2-y0y0MYHI0CWAFxOKcN9cCFUPF9gnh1";

    //Prod
    private static final String WEBHOOK = "https://discord.com/api/webhooks/866514841002967071/EkA9kDA80PvafbgvaS7VX5f35tR_EZpdFA7jhXZSXuWHnh_cEsk-RBoIPvrYkFQoUW9T";

    private final RateLimiter rateLimiter;

    public PiStatusListener(final RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void onStatus(Status status) {

        log.info("Mention by {}", status.getUser().getScreenName());
        rateLimiter.sendDiscordNotification(WEBHOOK, String.format(DiscordNotifier.TWEET_URL_FORMAT,
                status.getUser().getScreenName(), status.getId()));
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
