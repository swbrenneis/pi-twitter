package org.secomm.pitwitter.handlers;

import io.jsondb.JsonDBTemplate;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.Groups;
import org.secomm.pitwitter.config.Restocks;
import org.secomm.pitwitter.config.UserContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
@PropertySource("classpath:database.properties")
public class DatabaseHandler {

    @Value("${db.filesLocation}")
    private String dbFilesLocation;

    @Value("${db.baseScanPackage}")
    private String dbBaseScanPackage;

    private Global global;

    private Groups groups;

    private Restocks restocks;

    private JsonDBTemplate jsonDBTemplate;

    public void initialize() {

        jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, dbBaseScanPackage, null);
//        new DatabaseLoader().loadDatabase(jsonDBTemplate);

        global = jsonDBTemplate.findById("000001", Global.class);
        restocks = jsonDBTemplate.findById("000002", Restocks.class);
    }

    public List<UserContext> getGlobalUsers() {
        return global.getUsers();
    }

    public List<UserContext> getRestocksUsers() {
        return restocks.getUsers();
    }

    public void updateGlobalSearchTime(String username, String searchTime) {

        for (UserContext userContext : global.getUsers()) {
            if (userContext.getName().equals(username)) {
                userContext.setLastSearched(searchTime);
                jsonDBTemplate.upsert(global);
            }
        }
    }

    public void updateRestocksSearchTime(String username, String searchTime) {

        for (UserContext userContext : restocks.getUsers()) {
            if (userContext.getName().equals(username)) {
                userContext.setLastSearched(searchTime);
                jsonDBTemplate.upsert(restocks);
            }
        }
    }

    public void addUser(String username) {
        SimpleDateFormat format = new SimpleDateFormat(TwitterManager.DATE_FORMAT);
        UserContext userContext = new UserContext(username, format.format(new Date()));
        global.getUsers().add(userContext);
        jsonDBTemplate.upsert(global);
    }

    public void deleteUser(String username) {

        List<UserContext> userContextList = new ArrayList<>();
        for (UserContext userContext : global.getUsers()) {
            if (!userContext.getName().equals(username)) {
                userContextList.add(userContext);
            }
        }
        global.setUsers(userContextList);
        jsonDBTemplate.upsert(global);
    }

    public List<String> getGlobalTerms() {
        return global.getTerms();
    }

    public List<String> getRestocksTerms() {
        return restocks.getTerms();
    }

    public void addTerm(String term) {
        global.getTerms().add(term);
        jsonDBTemplate.upsert(global);
    }

    public void deleteTerm(String term) {
        List<String> termsList = new ArrayList<>();
        for (String termString : global.getTerms()) {
            if (!termString.equals(term)) {
                termsList.add(termString);
            }
        }
        global.setTerms(termsList);
        jsonDBTemplate.upsert(global);
    }

    public String getGlobalWebhook() {
        return global.getWebhook();
    }

    public String getRestocksWebhook() {
        return restocks.getRestockWebhook();
    }

    public String getGiveawayWebhook() {
        return restocks.getGiveawayWebhook();
    }

    public void setWebhook(String webhook) {
        global.setWebhook(webhook);
        jsonDBTemplate.upsert(global);
    }

    public Global getGlobal() {
        return global;
    }

    public Restocks getRestocks() {
        return restocks;
    }
}
