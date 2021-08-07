package org.secomm.pitwitter.module;

import twitter4j.Status;

import java.util.List;

public interface TwitterModule extends Runnable {

    void receivedStatuses(List<Status> statuses);
}
