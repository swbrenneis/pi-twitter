package org.secomm.pitwitter.loaders;

import com.mongodb.client.MongoCollection;
import io.jsondb.JsonDBTemplate;
import org.bson.Document;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:database.properties")
public class GlobalDatabaseLoader {

    @Value("${db.mongodbUrl}")
    private String mongodbUrl;

    @Value("${db.filesLocation}")
    private String dbFilesLocation;

    @Value("${db.baseScanPackage}")
    private String dbBaseScanPackage;

    public void loadDatabase(MongoCollection<Document> globalCollection) {

        try {
            JsonDBTemplate jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, dbBaseScanPackage, null);
            Global global = jsonDBTemplate.findById("000001", Global.class);

            Document webhookItem = new Document("item", "webhook")
                    .append("webhook", global.getWebhook());
            globalCollection.insertOne(webhookItem);
            Document termsItem = new Document("item", "terms")
                    .append("terms", global.getTerms());
            globalCollection.insertOne(termsItem);
            Document usersItem = new Document("item", "users");
            List<Document> userList = new ArrayList<>();
            for (UserContext userContext : global.getUsers()) {
                Document document = new Document("name", userContext.getName())
                        .append("lastId", userContext.getLastId());
                userList.add(document);
            }
            usersItem.append("users", userList);
            globalCollection.insertOne(usersItem);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
