package org.secomm.pitwitter.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.secomm.pitwitter.model.UserContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class RestocksDatabaseHandler {

    private static final Logger log = LoggerFactory.getLogger(RestocksDatabaseHandler.class);

    private MongoDbConnector mongoDbConnector;

    private MongoCollection<Document> restocksCollection;

    public RestocksDatabaseHandler(final MongoDbConnector mongoDbConnector) {
        this.mongoDbConnector = mongoDbConnector;
    }

    public void initialize() {

        restocksCollection = mongoDbConnector.getRestocksCollection();
    }

    public String getRestocksWebhook() {

        Bson query = Filters.exists("restocksWebhook");
        Document webhookDocument = restocksCollection.find(query).first();
        if (webhookDocument != null) {
            return webhookDocument.getString("restocksWebhook");
        } else {
            return "";
        }
    }

    public void setRestocksWebhook(String webhook) {

        Bson query = Filters.exists("restocksWebhook");
        Bson updateOperation = Updates.set("restocksWebhook", webhook);
        UpdateResult updateResult = restocksCollection.updateOne(query, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("Restocks webhook was not updated");
        }
    }

    public String getGiveawaysWebhook() {

        Bson query = Filters.exists("giveawaysWebhook");
        Document webhookDocument = restocksCollection.find(query).first();
        if (webhookDocument != null) {
            return webhookDocument.getString("giveawaysWebhook");
        } else {
            return "";
        }
    }

    public void setGiveawaysWebhook(String webhook) {

        Bson query = Filters.exists("giveawaysWebhook");
        Bson updateOperation = Updates.set("giveawaysWebhook", webhook);
        UpdateResult updateResult = restocksCollection.updateOne(query, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("GiveawaysWebhook was not updated");
        }
    }

    public long getLastId(String username) {

        Bson query = Filters.eq("name", username);
        Document userDocument = restocksCollection.find(query).first();
        if (userDocument != null) {
            return userDocument.getLong("lastId");
        } else {
            return -1;
        }
    }

    public void updateLastId(String username, long lastId) {

        Bson query = Filters.eq("name", username);
        Bson updateOperation = Updates.set("lastId", lastId);
        UpdateResult updateResult = restocksCollection.updateOne(query, updateOperation);
        if (updateResult.getModifiedCount() == 0) {
            log.warn("{} lastId was not updated", username);
        }
    }

    public List<UserContext> getUsers() {

        Bson query = Filters.regex("name", "@\\w+");
        List<Document> userList = restocksCollection.find(query).into(new ArrayList<>());
        return userList.stream()
                .map(document -> new UserContext(document.getString("name"), document.getLong("lastId")))
                .collect(Collectors.toList());
    }

    public List<String> getTerms() {
        Bson query = Filters.exists("terms");
        return restocksCollection.find(query).first().getList("terms", String.class);
    }

    public List<String> getExclusions() {
        Bson query = Filters.exists("exclusions");
        return restocksCollection.find(query).first().getList("exclusions", String.class);
    }

    public List<String> getRequired() {
        Bson query = Filters.exists("required");
        return restocksCollection.find(query).first().getList("required", String.class);
    }

}
