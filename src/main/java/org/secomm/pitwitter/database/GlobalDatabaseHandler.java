package org.secomm.pitwitter.database;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import org.bson.Document;
import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.loaders.GlobalDatabaseLoader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:database.properties")
public class GlobalDatabaseHandler {

//    @Autowired
//    private GlobalDatabaseLoader globalDatabaseLoader;

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

        Document query = new Document("item", "webhook");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document webhook = cursor.iterator().next();
            return webhook.getString("webhook");
        } else {
            return "";
        }
    }

    public void setWebhook(String webhook) {

        Document query = new Document("item", "webhook");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document webhookDocument = cursor.iterator().next();
            webhookDocument.put("webhook", webhook);
            globalCollection.updateOne(query, webhookDocument);
        }
    }

    public long getLastId(String user) {

        Document userDocument = getUserDocument(user);
        if (userDocument != null) {
            return userDocument.getLong("lastId");
        } else {
            return -1;
        }
    }

    public void updateLastId(String name, long lastId) {

        String handle = "@" + name;
        Document query = new Document("item", "users");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document usersDocument = cursor.iterator().next();
            List<Document> userList = usersDocument.getList("users", Document.class);
            for (Document document : userList) {
                if (document.getString("name").equals(handle)) {
                    document.put("lastId", lastId);
                }
            }
            globalCollection.updateOne(query, usersDocument);
        }
    }

    public List<UserContext> getUsers() {

        Document query = new Document("item", "users");
        MongoIterable<Document> cursor = globalCollection.find(query);
        List<UserContext> userContexts = new ArrayList<>();
        if (cursor.iterator().hasNext()) {
            Document usersDocument = cursor.iterator().next();
            List<Document> userList = usersDocument.getList("users", Document.class);
            for (Document document : userList) {
                userContexts.add(new UserContext(document.getString("name"), document.getLong("lastId")));
            }
        }
        return userContexts;
    }

    private Document getUserDocument(String name) {

        String handle = "@" + name;
        Document query = new Document("item", "users");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            List<Document> userList = cursor.iterator().next().getList("users", Document.class);
            for (Document document : userList) {
                if (document.getString("name").equals(handle)) {
                    return document;
                }
            }
        }
        return null;
    }

    public List<String> getTerms() {

        Document query = new Document("item", "terms");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document termsDocument = cursor.iterator().next();
            return termsDocument.getList("terms", String.class);
        }
        return new ArrayList<>();
    }

    public void addUser(String name) {

        Document query = new Document("item", "users");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document usersDocument = cursor.iterator().next();
            List<Document> usersList = usersDocument.getList("users", Document.class);
            Document newUser = new Document("name", name)
                    .append("lastId", 0L);
            usersList.add(newUser);
            usersDocument.put("users", usersList);
            globalCollection.updateOne(query, usersDocument);
        }
    }

    public void deleteUser(String name) {

        Document query = new Document("item", "users");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document usersDocument = cursor.iterator().next();
            List<Document> usersList = usersDocument.getList("users", Document.class);
            List<Document> updated = new ArrayList<>();
            for (Document document : usersList) {
                if (!document.getString("name").equals(name)) {
                    updated.add(document);
                }
            }
            usersDocument.put("users", updated);
            globalCollection.updateOne(query, usersDocument);
        }
    }

    public void addTerm(String term) {

        Document query = new Document("item", "terms");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document termsDocument = cursor.iterator().next();
            List<String> terms = termsDocument.getList("terms", String.class);
            terms.add(term);
            termsDocument.put("terms", terms);
            globalCollection.updateOne(query, termsDocument);
        }
    }

    public void deleteTerm(String term) {

        Document query = new Document("item", "terms");
        MongoIterable<Document> cursor = globalCollection.find(query);
        if (cursor.iterator().hasNext()) {
            Document termsDocument = cursor.iterator().next();
            List<String> updated = new ArrayList<>();
            List<String> terms = termsDocument.getList("terms", String.class);
            for (String test : terms) {
                if (!test.equals(term)) {
                    updated.add(test);
                }
            }
            termsDocument.put("terms", updated);
            globalCollection.updateOne(query, termsDocument);
        }
    }
}
