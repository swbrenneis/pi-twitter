package org.secomm.pitwitter.module;

import org.secomm.pitwitter.connectors.TwitterConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import twitter4j.FilterQuery;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;

@Component
@PropertySource("classpath:twitter4j.properties")
public class MentionsModule {

    private static final Logger log = LoggerFactory.getLogger(MentionsModule.class);

    @Value("${stream.track}")
    private String streamTrack;

    private final PiStatusListener piStatusListener;

    private final TwitterConnector twitterConnector;

    private TwitterStream stream;

    public MentionsModule(final PiStatusListener piStatusListener,
                          final TwitterConnector twitterConnector) {
        this.piStatusListener = piStatusListener;
        this.twitterConnector = twitterConnector;
    }

    public void initialize() {

        TwitterStreamFactory factory = new TwitterStreamFactory(twitterConnector.getConfiguration());
        stream = factory.getInstance();
        stream.addListener(piStatusListener);
        FilterQuery filterQuery = new FilterQuery();
        filterQuery.track(streamTrack);
        stream.filter(filterQuery);

        log.info("Mentions handler initialized");
    }


}
