package org.secomm.pitwitter.rest;

import org.secomm.pitwitter.module.MatchModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("twitter-config")
public class TwitterController {

    private final Logger log = LoggerFactory.getLogger(TwitterController.class);

    private final MatchModule twitterHandler;

    public TwitterController(final MatchModule matchHandler) {
        this.twitterHandler = matchHandler;
    }

    @GetMapping(value = "/get-global", produces = "application/json")
    @CrossOrigin
    public GlobalValues getGlobal() {
//        return getGlobalValues(twitterHandler.getGlobal());
        return new GlobalValues();
    }

    @PostMapping(value = "/edit-global-user", consumes = "application/json", produces = "application/json")
    @CrossOrigin
    public EditResponse editGlobalUsers(@RequestBody EditList edits) {

        log.debug("Edit global user request received: {}", edits);

        String result = null;
        switch (edits.getEditAction()) {
            case ADD:
                result = twitterHandler.editUser(edits.getEdits().get(0), MatchModule.Operation.ADD);
                break;
            case DELETE:
                result = twitterHandler.editUser(edits.getEdits().get(0), MatchModule.Operation.DELETE);
                break;
        }
//        return new EditResponse(result, getGlobalValues(twitterHandler.getGlobal()));
        return new EditResponse();
    }

    @PostMapping(value = "/edit-global-terms", consumes = "application/json", produces = "application/json")
    @CrossOrigin
    public EditResponse editGlobalTerms(@RequestBody EditList edits) {

        log.debug("Edit global terms request received: {}", edits);

        String result = null;
        switch (edits.getEditAction()) {
            case ADD:
                result = twitterHandler.editTerms(edits.getEdits(), MatchModule.Operation.ADD);
                break;
            case DELETE:
                result = twitterHandler.editTerms(edits.getEdits(), MatchModule.Operation.DELETE);
                break;
        }
//        return new EditResponse(result, getGlobalValues(twitterHandler.getGlobal()));
        return new EditResponse();
    }

    @PostMapping(value = "/edit-global-webhook", consumes = "application/json", produces = "application/json")
    @CrossOrigin
    public GlobalValues editGlobalWebhook(@RequestBody EditWebhook edit) {

        log.debug("Edit global webhook request received: {}", edit);

        twitterHandler.setWebhook(edit.getWebhook());

//        return getGlobalValues(twitterHandler.getGlobal());
        return new GlobalValues();
    }

}
