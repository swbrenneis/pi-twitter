package org.secomm.pitwitter.module;

import org.secomm.pitwitter.model.UserContext;
import org.springframework.stereotype.Component;
import twitter4j.Status;

import java.util.List;

@Component
public class FollowModule extends AbstractTwitterModule {

    protected FollowModule(RateLimiter rateLimiter) {
        super(rateLimiter);
    }

    @Override
    public void receivedStatuses(List<Status> statuses, UserContext userContext) {
    }

    @Override
    protected List<UserContext> getUsers() {
        return null;
    }
}
