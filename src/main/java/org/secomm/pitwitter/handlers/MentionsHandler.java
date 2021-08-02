package org.secomm.pitwitter.handlers;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.Configuration;

@Component
@PropertySource("classpath:twitter4j.properties")
public class MentionsHandler {

    @Value("${stream.track}")
    private String streamTrack;

    private final PiStatusListener piStatusListener;

    private final TwitterManager twitterManager;

    private TwitterStream stream;

    public MentionsHandler(final PiStatusListener piStatusListener,
                           final TwitterManager twitterManager) {
        this.piStatusListener = piStatusListener;
        this.twitterManager = twitterManager;
    }

    public void initialize() {

        TwitterStreamFactory factory = new TwitterStreamFactory(twitterManager.getConfiguration());
        stream = factory.getInstance();
        stream.addListener(piStatusListener);
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track(streamTrack);
        stream.filter(filterQuery);
    }


}
