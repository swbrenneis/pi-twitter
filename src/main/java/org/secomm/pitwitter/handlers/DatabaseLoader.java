package org.secomm.pitwitter.handlers;

import io.jsondb.JsonDBTemplate;
import org.secomm.pitwitter.config.Restocks;
import org.secomm.pitwitter.config.UserContext;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

public class DatabaseLoader {

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

    public void loadDatabase(JsonDBTemplate jsonDBTemplate) {

        try {

            Restocks restocks = new Restocks();
            restocks.setTerms(new ArrayList<>());
            restocks.setUsers(new ArrayList<>());

            BufferedReader reader = new BufferedReader(new FileReader("Bot_Restock_Accounts.txt"));
            String username = reader.readLine();
            while (username != null) {
                UserContext userContext = new UserContext(username, "31-07-2021 22:14:37");
                restocks.getUsers().add(userContext);
                username = reader.readLine();
            }
            restocks.getTerms().addAll(Arrays.asList(TERMS));
            restocks.setRestockWebhook(RESTOCK_WEBHOOK);
            restocks.setGiveawayWebhook(GIVEAWAY_WEBHOOK);
            jsonDBTemplate.createCollection(Restocks.class);
            jsonDBTemplate.upsert(restocks);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
