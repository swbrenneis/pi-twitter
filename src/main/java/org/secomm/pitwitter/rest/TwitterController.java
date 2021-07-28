package org.secomm.pitwitter.rest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.secomm.pitwitter.config.Global;
import org.secomm.pitwitter.config.UserContext;
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

    private final TwitterHandler twitterHandler;

    public TwitterController(final TwitterHandler twitterHandler) {
        this.twitterHandler = twitterHandler;
    }

    @GetMapping(value = "/get-global", produces = "application/json")
    @CrossOrigin
    public GlobalValues getGlobal() {
        return getGlobalValues(twitterHandler.getGlobal());
    }

    @PostMapping(value = "/edit-global-user", consumes = "application/json", produces = "application/json")
    @CrossOrigin
    public EditResponse editGlobalUsers(@RequestBody EditList edits) {

        log.debug("Edit global user request received: {}", edits);

        String result = null;
        switch (edits.getEditAction()) {
            case ADD:
                result = twitterHandler.editUser(edits.getEdits().get(0), TwitterHandler.Operation.ADD);
                break;
            case DELETE:
                result = twitterHandler.editUser(edits.getEdits().get(0), TwitterHandler.Operation.DELETE);
                break;
        }
        return new EditResponse(result, getGlobalValues(twitterHandler.getGlobal()));
    }

    @PostMapping(value = "/edit-global-terms", consumes = "application/json", produces = "application/json")
    @CrossOrigin
    public EditResponse editGlobalTerms(@RequestBody EditList edits) {

        log.debug("Edit global terms request received: {}", edits);

        String result = null;
        switch (edits.getEditAction()) {
            case ADD:
                result = twitterHandler.editTerms(edits.getEdits(), TwitterHandler.Operation.ADD);
                break;
            case DELETE:
                result = twitterHandler.editTerms(edits.getEdits(), TwitterHandler.Operation.DELETE);
                break;
        }
        return new EditResponse(result, getGlobalValues(twitterHandler.getGlobal()));
    }

    @PostMapping(value = "/edit-global-webhook", consumes = "application/json", produces = "application/json")
    @CrossOrigin
    public GlobalValues editGlobalWebhook(@RequestBody EditWebhook edit) {

        log.debug("Edit global webhook request received: {}", edit);

        twitterHandler.setWebhook(edit.getWebhook());

        return getGlobalValues(twitterHandler.getGlobal());
    }

    private GlobalValues getGlobalValues(Global global) {

        List<String> users = new ArrayList<>();
        for (UserContext userContext : global.getUsers()) {
            users.add(userContext.getName());
        }
        return new GlobalValues(users, global.getTerms(), global.getWebhook());
    }
}
