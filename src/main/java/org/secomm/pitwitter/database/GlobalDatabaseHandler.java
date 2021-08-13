package org.secomm.pitwitter.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.connectors.MongoDbConnector;
import org.secomm.pitwitter.loaders.GlobalDatabaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
@PropertySource("classpath:database.properties")
public class GlobalDatabaseHandler {

    private final Logger log = LoggerFactory.getLogger(GlobalDatabaseHandler.class);

    @Autowired
    private GlobalDatabaseLoader globalDatabaseLoader;

    private final MongoDbConnector mongoDbConnector;

    private MongoCollection<Document> globalCollection;

    public GlobalDatabaseHandler(final MongoDbConnector mongoDbConnector) {
        this.mongoDbConnector = mongoDbConnector;
    }

    public void initialize() {

        globalCollection = mongoDbConnector.getGlobalCollection();
//        globalDatabaseLoader.loadDatabase(globalCollection);
    }

    public String getWebhook() {

        Bson query = Filters.exists("webhook");
        Document webhookDocument = globalCollection.find(query).first();
        if (webhookDocument != null) {
            return webhookDocument.getString("webhook");
        } else {
            return "";
        }
    }

    public void setWebhook(String webhook) {

        Bson query = Filters.exists("webhook");
        Bson updateOperation = Updates.set("webhook", webhook);
        UpdateResult updateResult = globalCollection.updateOne(query, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("Webhook was not updated");
        }
    }

    public long getLastId(String username) {

        Bson query = Filters.eq("name", String.format("@%s", username));
        Document userDocument = globalCollection.find(query).first();
        if (userDocument != null) {
            return userDocument.getLong("lastId");
        } else {
            return -1;
        }
    }

    public void updateLastId(String username, long lastId) {

        Bson query = Filters.eq("name", String.format("@%s", username));
        Bson updateOperation = Updates.set("lastId", lastId);
        UpdateResult updateResult = globalCollection.updateOne(query, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("{} lastId was not updated", username);
        }
    }

    public List<UserContext> getUsers() {

        Bson query = Filters.regex("name", "@\\w+");
        List<Document> userList = globalCollection.find(query).into(new ArrayList<>());
        return userList.stream()
                .map(document -> new UserContext(document.getString("name"), document.getLong("lastId")))
                .collect(Collectors.toList());
    }

    public List<String> getTerms() {
        Bson query = Filters.exists("terms");
        return globalCollection.find(query).first().getList("terms", String.class);
    }

    public void addUser(String username) {
        globalCollection.insertOne(new Document("name", username).append("lastId", 0));
    }

    public void deleteUser(String username) {
        Bson query = Filters.eq("name", username);
        DeleteResult deleteResult = globalCollection.deleteOne(query);
        if (deleteResult.getDeletedCount() == 0) {
            log.warn("User {} was not deleted", username);
        }
    }

    public void addTerm(String term) {

        Bson query = Filters.exists("terms");
        List<String> terms = getTerms();
        Bson updateOperation = Updates.set("terms", terms);
        UpdateResult updateResult = globalCollection.updateOne(query, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("{} was not added to terms", term);
        }
    }

    public void deleteTerm(String term) {

        List<String> terms = getTerms();
        List<String> updated = new ArrayList<>();
        for (String test : terms) {
            if (!test.equals(term)) {
                updated.add(test);
            }
        }
        Bson filter = Filters.exists("terms");
        Bson updateOperation = Updates.set("terms", terms);
        UpdateResult updateResult = globalCollection.updateOne(filter, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("{} was not deleted from terms", term);
        }
    }
}
