package org.secomm.pitwitter.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.secomm.pitwitter.connectors.MongoDbConnector;
import org.secomm.pitwitter.loaders.RestocksDatabaseLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class RestockDatabaseHandler {

    private static final Logger log = LoggerFactory.getLogger(RestockDatabaseHandler.class);

    @Autowired
    private RestocksDatabaseLoader restocksDatabaseLoader;

    private MongoCollection<Document> restocksCollection;

    public void initialize(MongoDbConnector mongoDbConnector) {

        restocksCollection = mongoDbConnector.getRestocksCollection();
        restocksDatabaseLoader.loadDatabase(restocksCollection);
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
    
}
