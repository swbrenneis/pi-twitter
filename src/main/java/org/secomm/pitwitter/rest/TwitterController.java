package org.secomm.pitwitter.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.User;
import org.secomm.pitwitter.handlers.TwitterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("twitter-config")
public class TwitterController {

    private final Logger log = LoggerFactory.getLogger(TwitterController.class);

    private static final String CORS_HOST = "http://estaqueesta.net";

    private final TwitterHandler twitterHandler;

    private Gson gson = new GsonBuilder().create();

    public TwitterController(final TwitterHandler twitterHandler) {
        this.twitterHandler = twitterHandler;
    }

    @GetMapping(value = "/get-global", produces = "application/json")
    @CrossOrigin(origins = CORS_HOST)
    public GlobalValues getGlobal() {
        return getGlobalValues(twitterHandler.getGlobal());
    }

    @PostMapping(value = "/edit-global-users", consumes = "application/json", produces = "application/json")
    @CrossOrigin(origins = CORS_HOST)
    public GlobalValues editGlobalUsers(@RequestBody EditList edits) {

        log.debug("Edit global users request received: {}", edits);

        switch (edits.getEditAction()) {
            case ADD:
                twitterHandler.editUsers(edits.getEdits(), TwitterHandler.Operation.ADD);
                break;
            case DELETE:
                twitterHandler.editUsers(edits.getEdits(), TwitterHandler.Operation.DELETE);
                break;
        }
        return getGlobalValues(twitterHandler.getGlobal());
    }

    @PostMapping(value = "/edit-global-terms", consumes = "application/json", produces = "application/json")
    @CrossOrigin(origins = CORS_HOST)
    public GlobalValues editGlobalTerms(@RequestBody EditList edits) {

        log.debug("Edit global terms request received: {}", edits);

        switch (edits.getEditAction()) {
            case ADD:
                twitterHandler.editTerms(edits.getEdits(), TwitterHandler.Operation.ADD);
                break;
            case DELETE:
                twitterHandler.editTerms(edits.getEdits(), TwitterHandler.Operation.DELETE);
                break;
        }
        return getGlobalValues(twitterHandler.getGlobal());
    }

    @PostMapping(value = "/edit-global-webhook", consumes = "application/json", produces = "application/json")
    @CrossOrigin(origins = CORS_HOST)
    public GlobalValues editGlobalWebhook(@RequestBody EditWebhook edit) {

        log.debug("Edit global webhook request received: {}", edit);

        twitterHandler.setWebhook(edit.getWebhook());

        return getGlobalValues(twitterHandler.getGlobal());
    }

    private GlobalValues getGlobalValues(Global global) {

        List<String> users = new ArrayList<>();
        for (User user : global.getUsers()) {
            users.add(user.getName());
        }
        return new GlobalValues(users, global.getTerms(), global.getWebhook());
    }
}
