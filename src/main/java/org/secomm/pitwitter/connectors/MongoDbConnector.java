package org.secomm.pitwitter.connectors;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
@PropertySource("classpath:twitter4j.properties")
public class MongoDbConnector {

    @Value("${db.mongodbUrl}")
    private String mongodbUrl;

    private MongoClient mongoClient;

    private MongoDatabase mongoDatabase;

    private MongoCollection<Document> globalCollection;

    private MongoCollection<Document> restocksCollection;

    public void initialize() {

        MongoClientURI uri = new MongoClientURI(mongodbUrl);
        mongoClient = new MongoClient(uri);
        mongoDatabase = mongoClient.getDatabase("twitter");
        globalCollection = mongoDatabase.getCollection("global");
        restocksCollection = mongoDatabase.getCollection("restock");
    }

    public MongoCollection<Document> getGlobalCollection() {
        return globalCollection;
    }

    public MongoCollection<Document> getRestocksCollection() {
        return restocksCollection;
    }
}