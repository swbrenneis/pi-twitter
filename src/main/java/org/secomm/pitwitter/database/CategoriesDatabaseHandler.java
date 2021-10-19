package org.secomm.pitwitter.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.secomm.pitwitter.connectors.MongoDbConnector;
import org.secomm.pitwitter.model.Category;
import org.secomm.pitwitter.model.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class CategoriesDatabaseHandler {

    private static final Logger log = LoggerFactory.getLogger(CategoriesDatabaseHandler.class);

    private final MongoDbConnector mongoDbConnector;

    private MongoCollection<Document> categoriesCollection;

    public CategoriesDatabaseHandler(final MongoDbConnector mongoDbConnector) {
        this.mongoDbConnector = mongoDbConnector;
    }

    public void initialize() {

        categoriesCollection = mongoDbConnector.getCategoriesCollection();
    }

    public List<String> getCategories() {
        Bson query = Filters.regex("categories", "\\w+");
        return categoriesCollection.find(query).first().getList("categories", String.class);
    }

    public void addCategory(Category category) {

        List<Document> userList = new ArrayList<>();
        for (UserContext userContext : category.getUsers()) {
            Document userDocument = new Document("name", userContext.getName())
                    .append("lastId", userContext.getLastId());
            userList.add(userDocument);
        }
        Document categoryDocument = new Document("category", category.getCategory())
                .append("webhook", category.getWebhook())
                .append("terms", category.getTerms())
                .append("users", userList);
        categoriesCollection.insertOne(categoryDocument);
    }

    public List<UserContext> getUsers(String category) {

        Bson query = Filters.eq("category", category);
        Document categoryDocument = categoriesCollection.find(query).first();
        return categoryDocument.getList("users", Document.class).stream()
                .map(document -> new UserContext(document.getString("name"), document.getLong("lastId"), category))
                .collect(Collectors.toList());
   }

   public long getLastId(String username, String category) {

        List<UserContext> userContexts = getUsers(category);
        for (UserContext userContext : userContexts) {
            if (userContext.getName().equals(username)) {
                return userContext.getLastId();
            }
        }
        return -1;
   }

   public void updateLastId(String username, String category, long lastId) {

       Bson query = Filters.eq("category", category);
       Bson updateOperation = Updates.pull("users", new Document("name", username));
       UpdateResult updateResult = categoriesCollection.updateOne(query, updateOperation);
       if (updateResult.getModifiedCount() == 0) {
           log.warn("{} last ID not updated", username);
       } else {
           Document user = new Document("name", username)
                   .append("lastId", lastId)
                   .append("category", category);
           updateOperation = Updates.addToSet("users", user);
           updateResult = categoriesCollection.updateOne(query, updateOperation);
           if (updateResult.getModifiedCount() == 0) {
               log.warn("{} last ID not updated", username);
           }
       }
   }

    public List<String> getTerms(String category) {

        Bson query = Filters.eq("category", category);
        Document categoryDocument = categoriesCollection.find(query).first();
        return categoryDocument.getList("terms", String.class);
    }

    public List<String> getExclusions(String category) {

        Bson query = Filters.eq("category", category);
        Document categoryDocument = categoriesCollection.find(query).first();
        return categoryDocument.getList("exclusions", String.class);
    }

    public String getBncWebhook(String category) {

        Bson query = Filters.eq("category", category);
        Document categoryDocument = categoriesCollection.find(query).first();
        return categoryDocument.getString("bncWebHook");
    }

    public String getWickedWebhook(String category) {

        Bson query = Filters.eq("category", category);
        Document categoryDocument = categoriesCollection.find(query).first();
        return categoryDocument.getString("wickedWebHook");
    }
}
