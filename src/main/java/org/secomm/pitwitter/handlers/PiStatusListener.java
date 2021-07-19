package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.DiscordNotifier;
import org.springframework.stereotype.Component;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.User;

import java.util.ArrayList;

@Component
public class PiStatusListener implements StatusListener {

    private final DiscordNotifier discordNotifier;

    public PiStatusListener(final DiscordNotifier discordNotifier) {
        this.discordNotifier = discordNotifier;
    }

    @Override
    public void onStatus(Status status) {

        discordNotifier.sendWebhook("track", status.getUser(), status.getText(), new ArrayList<>());
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
