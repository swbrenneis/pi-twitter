package org.secomm.pitwitter.module;

import org.secomm.pitwitter.loaders.CategoriesDatabaseLoader;
import org.secomm.pitwitter.model.UserContext;
import org.secomm.pitwitter.database.CategoriesDatabaseHandler;
import org.springframework.stereotype.Component;
import twitter4j.Status;
import twitter4j.TwitterException;

import java.util.ArrayList;
import java.util.List;

@Component
public class CategoriesModule extends AbstractTwitterModule {

    private final CategoriesDatabaseHandler categoriesDatabaseHandler;

    protected CategoriesModule(final CategoriesDatabaseHandler categoriesDatabaseHandler,
                               final RateLimiter rateLimiter) {
        super(rateLimiter);
        this.categoriesDatabaseHandler = categoriesDatabaseHandler;
    }

    public void initialize() throws TwitterException {

        categoriesDatabaseHandler.initialize();
        CategoriesDatabaseLoader categoriesDatabaseLoader = new CategoriesDatabaseLoader();
        categoriesDatabaseLoader.loadDatabase(categoriesDatabaseHandler);
    }

    @Override
    public void receivedStatuses(List<Status> statuses, UserContext userContext) {

        boolean firstpass = true;
        for (Status status : statuses) {
            String category = userContext.getCategory();
            if (firstpass) {
                String screenName = statuses.get(0).getUser().getScreenName();
                log.info("{} statuses received for {}", statuses.size(), screenName);
                firstpass = false;
            }
            long lastId = categoriesDatabaseHandler.getLastId(userContext.getName(), category);
            if (status.getId() > lastId) {
                categoriesDatabaseHandler.updateLastId(userContext.getName(), category, status.getId());
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            List<String> terms = categoriesDatabaseHandler.getTerms(category);
            if (terms.size() == 1 && terms.contains("*")) {
                //Send everything
                sendNotification(categoriesDatabaseHandler.getBncWebhook(userContext.getCategory()), status);
                String wickedWebhook = categoriesDatabaseHandler.getWickedWebhook(userContext.getCategory());
                if (wickedWebhook != null) {
                    sendNotification(wickedWebhook, status);
                }
            } else {
                // Strip out mentions
                String stripped = tweet.replaceAll("@\\w+", "");
                for (String term : terms) {
                    if (!notificationSent && included(stripped, category) && !excluded(stripped, category)) {
                        log.info("{} matched {}", status.getUser().getScreenName(), term);
                        sendNotification(categoriesDatabaseHandler.getBncWebhook(userContext.getCategory()), status);
                        String wickedWebhook = categoriesDatabaseHandler.getWickedWebhook(userContext.getCategory());
                        if (wickedWebhook != null) {
                            sendNotification(wickedWebhook, status);
                        }
                        notificationSent = true;
                    }
                }
            }
        }
    }

    private boolean excluded(String tweet, String category) {

        List<String> exclusions = categoriesDatabaseHandler.getExclusions(category);
        for (String exclusion : exclusions) {
            if (tweet.toUpperCase().contains(exclusion.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    private boolean included(String tweet, String category) {

        List<String> terms = categoriesDatabaseHandler.getTerms(category);
        for (String term : terms) {
            if (tweet.toUpperCase().contains(term.toUpperCase())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected List<UserContext> getUsers() {
        List<UserContext> users = new ArrayList<>();
        List<String> categories = categoriesDatabaseHandler.getCategories();
        for (String category : categories) {
            List<UserContext> categoryUsers = categoriesDatabaseHandler.getUsers(category);
            users.addAll(categoryUsers);
        }
        return users;
    }
}
