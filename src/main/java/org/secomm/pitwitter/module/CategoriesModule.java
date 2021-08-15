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
            if (firstpass) {
                String screenName = statuses.get(0).getUser().getScreenName();
                log.info("{} statuses received for {}", statuses.size(), screenName);
                firstpass = false;
            }
            long lastId = categoriesDatabaseHandler.getLastId(userContext.getName(), userContext.getCategory());
            if (status.getId() > lastId) {
                categoriesDatabaseHandler.updateLastId(userContext.getName(), userContext.getCategory(), status.getId());
            }
            String tweet = status.getText();
            boolean notificationSent = false;
            List<String> terms = categoriesDatabaseHandler.getTerms(userContext.getCategory());
            for (String term : terms) {
                if (!notificationSent && tweet.toUpperCase().contains(term.toUpperCase())) {
                    log.info("{} matched {}", status.getUser().getScreenName(), term);
                    sendNotification(categoriesDatabaseHandler.getWebhook(userContext.getCategory()), status);
                    notificationSent = true;
                }
            }
        }
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
