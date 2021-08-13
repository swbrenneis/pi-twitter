package org.secomm.pitwitter.loaders;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import org.secomm.pitwitter.config.Restocks;
import org.secomm.pitwitter.config.UserContext;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@Component
public class RestocksDatabaseLoader {

    private static final String RESTOCK_WEBHOOK = "https://discord.com/api/webhooks/871412406386053131/wxte26UjP7xSWFnmGoYpqGD6_ot3Fm7Bi6MQXTi-DaRkyTZ62g9jEctuiBj4-YOtGz4u";

    private static final String GIVEAWAY_WEBHOOK = "https://discord.com/api/webhooks/871556401296732270/OF1M9JqV1TxFZjGpPnCY0seu3TvMkt_vLU0-UdgD8ljPWhiZRhFkRJ-Q_o0GatusF4rV";

    private static final String[] TERMS = {
            "giveaway",
            "give away",
            "giving away",
            "contest",
            "daily",
            "dailies",
            "weekly",
            "weeklies",
            "monthly",
            "monthlies"
    };

    public void loadDatabase(MongoCollection<Document> restocksCollection) {

        try {

            Restocks restocks = new Restocks();
            restocks.setTerms(new ArrayList<>());
            restocks.setUsers(new ArrayList<>());
            restocks.setExcludes(new ArrayList<>());

            BufferedReader reader = new BufferedReader(new FileReader("Bot_Restock_Accounts.txt"));
            String username = reader.readLine();
            while (username != null) {
                UserContext userContext = new UserContext(username, 0);
                restocks.getUsers().add(userContext);
                username = reader.readLine();
            }
            restocks.getTerms().addAll(Arrays.asList(TERMS));
            restocks.setRestockWebhook(RESTOCK_WEBHOOK);
            restocks.setGiveawayWebhook(GIVEAWAY_WEBHOOK);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
