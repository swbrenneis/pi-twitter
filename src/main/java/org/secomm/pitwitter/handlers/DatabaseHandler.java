package org.secomm.pitwitter.handlers;

import io.jsondb.JsonDBTemplate;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.Groups;
import org.secomm.pitwitter.config.User;
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

    private JsonDBTemplate jsonDBTemplate;

    public void initialize() {

        jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, dbBaseScanPackage, null);
        global = jsonDBTemplate.findById("000001", Global.class);
//        global = new Global("000001", new ArrayList<>(), new ArrayList<>());
//        jsonDBTemplate.upsert(global);
    }

    public List<User> getUsers() {
        return global.getUsers();
    }

    public void updateSearchTime(String username, String searchTime) {

        for (User user : global.getUsers()) {
            if (user.getName().equals(username)) {
                user.setLastSearched(searchTime);
                jsonDBTemplate.upsert(global);
            }
        }
    }

    public void addUser(String username) {
        SimpleDateFormat format = new SimpleDateFormat(TwitterHandler.DATE_FORMAT);
        User user = new User(username, format.format(new Date()));
        global.getUsers().add(user);
        jsonDBTemplate.upsert(global);
    }

    public void deleteUser(String username) {

        List<User> userList = new ArrayList<>();
        for (User user : global.getUsers()) {
            if (!user.getName().equals(username)) {
                userList.add(user);
            }
        }
        global.setUsers(userList);
        jsonDBTemplate.upsert(global);
    }

    public List<String> getTerms() {
        return global.getTerms();
    }

    public void addTerm(String term) {
        global.getTerms().add(term);
        jsonDBTemplate.upsert(global);
    }

    public void deleteTerm(String term) {
        List<String> termsList = new ArrayList<>();
        for (String termString : global.getTerms()) {
            if (!termString.equals(term)) {
                termsList.add(term);
            }
        }
        global.setTerms(termsList);
        jsonDBTemplate.upsert(global);
    }

    public String getWebhook() {
        return global.getWebhook();
    }

    public void setWebhook(String webhook) {
        global.setWebhook(webhook);
        jsonDBTemplate.upsert(global);
    }

    public Global getGlobal() {
        return global;
    }
}
