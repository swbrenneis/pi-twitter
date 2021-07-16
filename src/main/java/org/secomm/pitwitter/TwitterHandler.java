package org.secomm.pitwitter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.Groups;
import org.secomm.pitwitter.config.User;
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
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

@Component
@PropertySource("classpath:twitter4j.properties")
public class TwitterHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TwitterHandler.class);

    @Value("${oauth.consumerKey}")
    private String consumerKey;

    @Value("${oauth.consumerSecret}")
    private String consumerSecret;

    @Value("${oauth.accessToken}")
    private String accessToken;

    @Value("${oauth.accessTokenSecret}")
    private String accessTokenSecret;

    private final DiscordNotifier discordNotifier;

    private Twitter twitter;

    private Global global;

    private Groups groups;

    private final Gson gson;

    public TwitterHandler(final DiscordNotifier discordNotifier) {
        this.discordNotifier = discordNotifier;
        gson = new GsonBuilder().create();
    }

    public void initialize() throws FileNotFoundException, URISyntaxException {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret);
        TwitterFactory twitterFactory = new TwitterFactory(configurationBuilder.build());
        twitter = twitterFactory.getInstance();

        URI uri = getClass().getResource("global.json").toURI();
        global = gson.fromJson(new FileReader(Paths.get(uri).toFile()), Global.class);

        uri = getClass().getResource("groups.json").toURI();
        groups = gson.fromJson(new FileReader(Paths.get(uri).toFile()), Groups.class);
    }

    @Override
    public void run() {

        try {
            initialize();

            for (User user : global.getUsers()) {
                List<Status> timeline = getUserTimeline(user);
                searchTimeline(timeline, global.getSearches());
            }

            storeConfig();
        } catch (TwitterException e) {
            log.error("Twitter error: {}", e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Configuration error: {}", e.getLocalizedMessage());
        }
    }

    private List<Status> getUserTimeline(User user) throws Exception {

        log.debug("Getting 100 statuses for user {}", user.getName());
        Paging paging = new Paging();
        paging.setCount(50);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date lastCreated = format.parse(user.getLastSearched());
        List<Status> statusList = twitter.getUserTimeline(user.getName(), paging);
        List<Status> timeline = new ArrayList<>();

        for (Status status : statusList) {
            Date created = status.getCreatedAt();
            if (created.after(lastCreated)) {
                timeline.add(status);
            }
        }
        user.setLastSearched(format.format(new Date()));

        return timeline;
    }

    private void searchTimeline(List<Status> timeline, List<String> terms) {

        for (Status status : timeline) {
            String tweet = status.getText();
            boolean notified = false;
            for (int i = 0; i < terms.size() && !notified; i++) {
                String term = terms.get(i);
                if (tweet.toUpperCase().contains(term.toUpperCase())) {
                    sendNotification(status);
                    notified = true;
                }
            }
        }
    }

    private void sendNotification(Status status) {

    }

    private void storeConfig() throws URISyntaxException, IOException {

        URI uri = getClass().getResource("global.json").toURI();
        String json = gson.toJson(global);
        Files.writeString(Paths.get(uri).toAbsolutePath(), json);

        uri = getClass().getResource("groups.json").toURI();
        json = gson.toJson(groups);
        Files.writeString(Paths.get(uri).toAbsolutePath(), json);
    }
}
