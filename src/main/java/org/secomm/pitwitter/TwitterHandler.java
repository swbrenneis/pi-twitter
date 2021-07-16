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
import twitter4j.*;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

@Component
@PropertySource("classpath:twitter4j.properties")
public class TwitterHandler {

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

    private Map<String, twitter4j.User> userMap;

    public TwitterHandler(final DiscordNotifier discordNotifier) {
        this.discordNotifier = discordNotifier;
        gson = new GsonBuilder().create();
        userMap = new HashMap<>();
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

        URI uri = getClass().getClassLoader().getResource("global.json").toURI();
        global = gson.fromJson(new FileReader(Paths.get(uri).toFile()), Global.class);

        uri = getClass().getClassLoader().getResource("groups.json").toURI();
        groups = gson.fromJson(new FileReader(Paths.get(uri).toFile()), Groups.class);
    }

    public void run() {

        try {
            loadUsers();
            for (User user : global.getUsers()) {
                List<Status> timeline = getUserTimeline(user);
                Status status = searchTimeline(timeline, global.getSearches());
                if (status != null) {
                    twitter4j.User twitterUser = userMap.get(user.getName());
                    sendNotification(twitterUser, status);
                }
            }

            storeConfig();
        } catch (TwitterException e) {
            log.error("Twitter error: {}", e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Configuration error: {}", e.getLocalizedMessage());
        }
    }

    private void loadUsers() throws TwitterException {

        List<String> usernames = new ArrayList<>();
        for (User user : global.getUsers()) {
            usernames.add(user.getName().substring(1));
        }
        ResponseList<twitter4j.User> twitterUsers = twitter.lookupUsers(usernames.toArray(new String[0]));
        for (int i = 0; i < usernames.size(); ++i) {
            userMap.put("@" + usernames.get(i), twitterUsers.get(i));
        }
    }

    private List<Status> getUserTimeline(User user) throws Exception {

        log.debug("Getting 50 statuses for user {}", user.getName());
        Paging paging = new Paging();
        paging.setCount(50);
        SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss");
        Date lastSearched = null;
        String lastSearchedString = user.getLastSearched();
        if (lastSearchedString != null) {
            lastSearched = format.parse(lastSearchedString);
        } else {
            lastSearched = new Date();
        }
        List<Status> statusList = twitter.getUserTimeline(user.getName(), paging);
        List<Status> timeline = new ArrayList<>();

        for (Status status : statusList) {
            Date created = status.getCreatedAt();
            if (created.after(lastSearched)) {
                timeline.add(status);
            }
        }
        user.setLastSearched(format.format(new Date()));

        return timeline;
    }

    private Status searchTimeline(List<Status> timeline, List<String> terms) {

        for (Status status : timeline) {
            String tweet = status.getText();
            for (String term : terms) {
                if (tweet.toUpperCase().contains(term.toUpperCase())) {
                    return status;
                }
            }
        }
        return null;
    }

    private void sendNotification(twitter4j.User user, Status status) {

        discordNotifier.sendWebhook(user, status.getText(), new ArrayList<>());
    }

    private void storeConfig() throws URISyntaxException, IOException {

        URI uri = getClass().getClassLoader().getResource("global.json").toURI();
        String json = gson.toJson(global);
        Files.writeString(Paths.get(uri).toAbsolutePath(), json);

        uri = getClass().getClassLoader().getResource("groups.json").toURI();
        json = gson.toJson(groups);
        Files.writeString(Paths.get(uri).toAbsolutePath(), json);
    }
}
