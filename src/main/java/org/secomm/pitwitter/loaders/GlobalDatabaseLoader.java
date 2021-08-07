package org.secomm.pitwitter.loaders;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.UserContext;

import java.util.ArrayList;
import java.util.List;

public class GlobalDatabaseLoader {

    public void loadDatabase(Global global) {

        try {
            MongoClientURI uri =
                    new MongoClientURI("mongodb://8v8b7NVZR5MI9pom:kH7DmbPzMwgiM4h7@estaqueesta.net/?authSource=admin");
            MongoClient mongoClient = new MongoClient(uri);
            MongoDatabase mongoDatabase = mongoClient.getDatabase("twitter");

            MongoCollection<Document> globalCollection = mongoDatabase.getCollection("global");
            Document webhook = new Document("webhook", global.getWebhook());
            globalCollection.insertOne(webhook);
            Document terms = new Document("terms", global.getTerms());
            globalCollection.insertOne(terms);
            List<Document> users = new ArrayList<>();
            for (UserContext userContext : global.getUsers()) {
                Document userDocument = new Document("name", userContext.getName())
                        .append("lastId", userContext.getLastId());
                users.add(userDocument);
            }
            Document userDocument = new Document("users", users);
            globalCollection.insertOne(userDocument);
            mongoClient.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
