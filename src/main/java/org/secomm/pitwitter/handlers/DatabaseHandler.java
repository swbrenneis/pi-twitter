package org.secomm.pitwitter.handlers;

import io.jsondb.JsonDBTemplate;
import org.secomm.pitwitter.config.Module;
import org.secomm.pitwitter.config.FollowContext;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.Groups;
import org.secomm.pitwitter.config.Modules;
import org.secomm.pitwitter.config.Restocks;
import org.secomm.pitwitter.config.UserContext;
import org.secomm.pitwitter.loaders.FollowDatabaseLoader;
import org.secomm.pitwitter.loaders.GlobalDatabaseLoader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@PropertySource("classpath:database.properties")
public class DatabaseHandler {

    public enum DatabaseSelector { GLOBAL, GROUP, RESTOCKS, GIVEAWAY }
    @Value("${db.filesLocation}")
    private String dbFilesLocation;

    @Value("${db.baseScanPackage}")
    private String dbBaseScanPackage;

    private Global global;

    private Groups groups;

    private Restocks restocks;

    private Modules modules;

    private JsonDBTemplate jsonDBTemplate;

    public void initialize() {

        jsonDBTemplate = new JsonDBTemplate(dbFilesLocation, dbBaseScanPackage, null);
//        new RestocksDatabaseLoader().loadDatabase(jsonDBTemplate);
//        new FollowDatabaseLoader().loadDatabase(jsonDBTemplate);

        global = jsonDBTemplate.findById("000001", Global.class);
        new GlobalDatabaseLoader().loadDatabase(global);
//        restocks = jsonDBTemplate.findById("000002", Restocks.class);
//        modules = jsonDBTemplate.findById("000003", Modules.class);
    }

    public List<UserContext> getUsers(DatabaseSelector selector) {
        switch (selector) {
            case GROUP:
                return null;
            case GLOBAL:
                return global.getUsers();
            case RESTOCKS:
                return restocks.getUsers();
            default:
                return null;
        }
    }

    public List<FollowContext> getFollowing(String name) {

        for (Module module : modules.getModules()) {
            if (module.getName().equals(name)) {
                return module.getFollowing();
            }
        }
        return null;
    }

    public void updateLastId(String username, long lastId, DatabaseSelector selector) {

        String userToCompare = "@" + username.toUpperCase();
        switch (selector) {
            case GROUP:
                break;
            case GLOBAL:
                updateGlobalLastId(userToCompare, lastId);
                break;
            case RESTOCKS:
                updateRestocksLastId(userToCompare, lastId);
                break;
        }
    }

    public void updateLastId(String username, long lastId, String modulename) {

        for (Module module : modules.getModules()) {
            if (module.getName().equals(modulename)) {
                for (FollowContext followContext : module.getFollowing()) {
                    if (followContext.getUsername().equals(username)) {
                        followContext.setLastId(lastId);
                        jsonDBTemplate.upsert(modules);
                    }
                }
            }
        }
    }

    private void updateGlobalLastId(String userToCompare, long lastId) {

        for (UserContext userContext : global.getUsers()) {
            if (userContext.getName().toUpperCase().equals(userToCompare)) {
                userContext.setLastId(lastId);
                jsonDBTemplate.upsert(global);
            }
        }
    }

    public long getLastId(String userName, DatabaseSelector selector) {

        String userToCompare = "@" + userName.toUpperCase();
        switch (selector) {
            case GROUP:
                return 0;
            case GLOBAL:
                return getGlobalLastId(userToCompare);
            case RESTOCKS:
                return getRestocksLastId(userToCompare);
            default:
                return 0;
        }
    }

    public long getLastId(String username, String modulename) {

        for (Module module : modules.getModules()) {
            if (module.getName().equals(modulename)) {
                for (FollowContext followContext : module.getFollowing()) {
                    if (followContext.getUsername().equals(username)) {
                        return followContext.getLastId();
                    }
                }
            }
        }
        return 0;
    }

    private long getGlobalLastId(String userToCompare) {

        for (UserContext userContext : global.getUsers()) {
            if (userContext.getName().toUpperCase().equals(userToCompare)) {
                return userContext.getLastId();
            }
        }
        return 0;
    }

    private long getRestocksLastId(String userToCompare) {

        for (UserContext userContext : restocks.getUsers()) {
            if (userContext.getName().toUpperCase().equals(userToCompare)) {
                return userContext.getLastId();
            }
        }
        return 0;
    }

    private void updateRestocksLastId(String userToCompare, long lastId) {

        for (UserContext userContext : restocks.getUsers()) {
            if (userContext.getName().toUpperCase().equals(userToCompare)) {
                userContext.setLastId(lastId);
                jsonDBTemplate.upsert(restocks);
            }
        }
    }

    public void addUser(String username) {
        UserContext userContext = new UserContext(username, 0);
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

    public List<String> getTerms(DatabaseSelector selector) {
        switch (selector) {
            case GROUP:
                return null;
            case GLOBAL:
                return global.getTerms();
            case RESTOCKS:
                return restocks.getTerms();
            default:
                return null;
        }
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

    public List<String> getExcludes() {
        return restocks.getExcludes();
    }

    public String getWebhook(DatabaseSelector selector) {
        switch (selector) {
            case GROUP:
                return null;
            case GLOBAL:
                return global.getWebhook();
            case RESTOCKS:
                return restocks.getRestockWebhook();
            case GIVEAWAY:
                return restocks.getGiveawayWebhook();
            default:
                return null;
        }
    }

    public String getWebhook(String username, String modulename) {

        for (Module module : modules.getModules()) {
            if (module.getName().equals(modulename)) {
                for (FollowContext followContext : module.getFollowing()) {
                    if (followContext.getUsername().equals(username)) {
                        return followContext.getWebhook();
                    }
                }
            }
        }
        return "";
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
