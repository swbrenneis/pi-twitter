package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.DiscordNotifier;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.webhook.Embed;
import org.secomm.pitwitter.webhook.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import twitter4j.*;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
@PropertySource("classpath:twitter4j.properties")
public class TwitterHandler {

    private static final Logger log = LoggerFactory.getLogger(TwitterHandler.class);

    public enum Operation { ADD, DELETE }

    public static final String DATE_FORMAT = "dd-MM-yyyy HH:mm:ss";

    private static final int PAGE_SIZE = 25;

    @Value("${oauth.consumerKey}")
    private String consumerKey;

    @Value("${oauth.consumerSecret}")
    private String consumerSecret;

    @Value("${oauth.accessToken}")
    private String accessToken;

    @Value("${oauth.accessTokenSecret}")
    private String accessTokenSecret;

    private String webhook;

    private final DiscordNotifier discordNotifier;

    private final DatabaseHandler databaseHandler;

    private final MentionsHandler mentionsHandler;

    private Twitter twitter;

    private final Map<String, twitter4j.User> userMap;

    public TwitterHandler(final DiscordNotifier discordNotifier,
                          final DatabaseHandler databaseHandler,
                          final MentionsHandler mentionsHandler) {
        this.discordNotifier = discordNotifier;
        this.databaseHandler = databaseHandler;
        this.mentionsHandler = mentionsHandler;
        userMap = new HashMap<>();
    }

    public void initialize() throws FileNotFoundException, URISyntaxException {

        ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
        configurationBuilder.setDebugEnabled(true)
                .setOAuthAccessToken(accessToken)
                .setOAuthAccessTokenSecret(accessTokenSecret)
                .setOAuthConsumerKey(consumerKey)
                .setOAuthConsumerSecret(consumerSecret);
        Configuration configuration = configurationBuilder.build();
        TwitterFactory twitterFactory = new TwitterFactory(configuration);
        twitter = twitterFactory.getInstance();

        databaseHandler.initialize();
        mentionsHandler.initialize(configuration);
    }

    public String editUser(String userName, Operation operation) {

        switch (operation) {
            case ADD:
                try {
                    User user = twitter.showUser(userName);
                    if (user == null) {
                        log.warn("User {} does not exist", userName);
                        return "No such user";
                    } else if (user.isProtected()) {
                        log.warn("User {} timeline is private", userName);
                        return "User timeline is private";
                    } else {
                        databaseHandler.addUser(userName);
                        return "User added";
                    }
                } catch (TwitterException e) {
                    log.error("Twitter exception in editUsers: {}", e.getLocalizedMessage());
                }
                break;
            case DELETE:
                databaseHandler.deleteUser(userName);
                return "User deleted";
        }
        return "Invalid operation";
    }

    public String editTerms(List<String> terms, Operation operation) {

        for (String term : terms) {
            switch (operation) {
                case ADD:
                    databaseHandler.addTerm(term);
                    break;
                case DELETE:
                    databaseHandler.deleteTerm(term);
            }
        }
        return "Success";
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
        databaseHandler.setWebhook(webhook);
    }

    public void run() {

        try {
            loadUsers();
            Lock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            boolean run = true;
            while (run) {
                for (UserContext userContext : databaseHandler.getUsers()) {
                    List<Status> timeline = getUserTimeline(userContext);
                    Status status = searchTimeline(timeline, databaseHandler.getTerms());
                    if (status != null) {
                        twitter4j.User twitterUser = userMap.get(userContext.getName());
                        sendNotification(twitterUser, status);
                    }
                }
                lock.lock();
                try {
                    run = !condition.await(1, TimeUnit.MINUTES);
                } finally {
                    lock.unlock();
                }
            }
        } catch (TwitterException e) {
            log.error("Twitter error: {}", e.getLocalizedMessage());
        } catch (Exception e) {
            log.error("Configuration error: {}", e.getLocalizedMessage());
        }
    }

    private void loadUsers() throws TwitterException {

        if (!databaseHandler.getUsers().isEmpty()) {
            List<String> usernames = new ArrayList<>();
            for (UserContext userContext : databaseHandler.getUsers()) {
                User user = twitter.showUser(userContext.getName().substring(1));
                if (user != null) {
                    userMap.put(userContext.getName(), user);
                } else {
                    log.warn("User {} does not exist", userContext.getName());
                }
            }
        }
    }

    private List<Status> getUserTimeline(UserContext userContext) throws Exception {

        log.debug("Getting {} statuses for user {}", PAGE_SIZE, userContext.getName());
        Paging paging = new Paging();
        paging.setCount(PAGE_SIZE);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        Date lastSearched = null;
        String lastSearchedString = userContext.getLastSearched();
        if (lastSearchedString != null) {
            lastSearched = format.parse(lastSearchedString);
        } else {
            lastSearched = new Date();
        }
        List<Status> statusList = twitter.getUserTimeline(userContext.getName(), paging);
        List<Status> timeline = new ArrayList<>();

        for (Status status : statusList) {
            Date created = status.getCreatedAt();
            if (created.after(lastSearched)) {
                timeline.add(status);
            }
        }
        databaseHandler.updateSearchTime(userContext.getName(), format.format(new Date()));

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
        discordNotifier.sendWebhook(webhook, user, status.getText(), embeds);
    }

    public Global getGlobal() {
        return databaseHandler.getGlobal();
    }
}
