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

            globalCollection.insertOne(new Document("webhook", global.getWebhook()));
            globalCollection.insertOne(new Document("terms", global.getTerms()));
            List<Document> userList = new ArrayList<>();
            for (UserContext userContext : global.getUsers()) {
                Document document = new Document("name", userContext.getName())
                        .append("lastId", userContext.getLastId());
                userList.add(document);
            }
            globalCollection.insertMany(userList);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
