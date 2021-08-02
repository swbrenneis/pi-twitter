package org.secomm.pitwitter.handlers;

import twitter4j.Status;

import java.util.List;

public interface TwitterHandler extends Runnable {

    void receivedStatuses(List<Status> statuses);
}
