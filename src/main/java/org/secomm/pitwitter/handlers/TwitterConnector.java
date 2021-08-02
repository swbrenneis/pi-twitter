package org.secomm.pitwitter.handlers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import twitter4j.Paging;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:twitter4j.properties")
public class TwitterConnector {

    public static final Logger log = LoggerFactory.getLogger(TwitterConnector.class);

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    @Value("${oauth.consumerKey}")
    private String consumerKey;

    @Value("${oauth.consumerSecret}")
    private String consumerSecret;

    @Value("${oauth.accessToken}")
    private String accessToken;

    @Value("${oauth.accessTokenSecret}")
    private String accessTokenSecret;

    private Twitter twitter;

    private Configuration configuration;

    public void initialize() throws FileNotFoundException, URISyntaxException {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret);
        configuration = configurationBuilder.build();
        TwitterFactory twitterFactory = new TwitterFactory(configuration);
        twitter = twitterFactory.getInstance();
    }

    public User showUser(String userName) throws TwitterException {
        return twitter.showUser(userName);
    }

    public List<Status> getUserTimeline(String userName, int pageSize, long lastId) {

        Paging paging = new Paging();
        paging.setCount(pageSize);
        if (lastId > 0) {
            paging.setSinceId(lastId);
        }
        try {
            List<Status> statusList = twitter.getUserTimeline(userName, paging);
            return statusList;
        } catch (TwitterException e) {
            log.warn("Twitter error while retrieving timeline for {}: {}", userName, e.getLocalizedMessage());
            return new ArrayList<>();
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }
}

