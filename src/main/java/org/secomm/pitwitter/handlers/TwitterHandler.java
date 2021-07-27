package org.secomm.pitwitter.handlers;

import org.secomm.pitwitter.DiscordNotifier;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import twitter4j.Paging;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
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

    public void editUsers(List<String> users, Operation operation) {

        for (String user : users) {
            switch (operation) {
                case ADD:
                    databaseHandler.addUser(user);
                    break;
                case DELETE:
                    databaseHandler.deleteUser(user);
            }
        }
    }

    public void editTerms(List<String> terms, Operation operation) {

        for (String term : terms) {
            switch (operation) {
                case ADD:
                    databaseHandler.addTerm(term);
                    break;
                case DELETE:
                    databaseHandler.deleteTerm(term);
            }
        }
    }

    public void setWebhook(String webhook) {
        this.webhook = webhook;
    }

    public void run() {

        try {
            loadUsers();
            Lock lock = new ReentrantLock();
            Condition condition = lock.newCondition();
            boolean run = true;
            while (run) {
                for (User user : databaseHandler.getUsers()) {
                    List<Status> timeline = getUserTimeline(user);
                    Status status = searchTimeline(timeline, databaseHandler.getTerms());
                    if (status != null) {
                        twitter4j.User twitterUser = userMap.get(user.getName());
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
            for (User user : databaseHandler.getUsers()) {
                usernames.add(user.getName().substring(1));
            }
            ResponseList<twitter4j.User> twitterUsers = twitter.lookupUsers(usernames.toArray(new String[0]));
            for (int i = 0; i < usernames.size(); ++i) {
                userMap.put("@" + usernames.get(i), twitterUsers.get(i));
            }
        }
    }

    private List<Status> getUserTimeline(User user) throws Exception {

        log.debug("Getting 50 statuses for user {}", user.getName());
        Paging paging = new Paging();
        paging.setCount(50);
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
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
        databaseHandler.updateSearchTime(user.getName(), format.format(new Date()));

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

        discordNotifier.sendWebhook(webhook, user, status.getText(), new ArrayList<>());
    }

    public Global getGlobal() {
        return databaseHandler.getGlobal();
    }
}