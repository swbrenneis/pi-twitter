package org.secomm.pitwitter.module;

import org.secomm.pitwitter.model.UserContext;
import twitter4j.Status;

import java.util.List;

public interface TwitterModule extends Runnable {

    void receivedStatuses(List<Status> statuses, UserContext userContext);
}
