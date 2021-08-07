package org.secomm.pitwitter;

import org.secomm.pitwitter.handlers.DatabaseHandler;
import org.secomm.pitwitter.module.FollowModule;
import org.secomm.pitwitter.module.MatchModule;
import org.secomm.pitwitter.module.MentionsModule;
import org.secomm.pitwitter.module.RateLimiter;
import org.secomm.pitwitter.module.RestockModule;
import org.secomm.pitwitter.handlers.TwitterConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class PiTwitter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PiTwitter.class);

    @Autowired
    private DatabaseHandler databaseHandler;

    @Autowired
    private MatchModule matchHandler;

    @Autowired
    private MentionsModule mentionsHandler;

    @Autowired
    private RestockModule restockHandler;

    @Autowired
    private FollowModule bncModule;

    @Autowired
    private TwitterConnector twitterConnector;

    @Autowired
    private RateLimiter rateLimiter;

    private ExecutorService executor;

    public static void main(String... args) {
        SpringApplication.run(PiTwitter.class);
    }

    @Override
    public void run(String... args) {

        executor = Executors.newFixedThreadPool(4);
        Lock lock;
        Condition condition;
        lock = new ReentrantLock();
        condition = lock.newCondition();

        try {
            databaseHandler.initialize();
            twitterConnector.initialize();
            matchHandler.initialize();
            mentionsHandler.initialize();
            restockHandler.initialize();
            bncModule.initialize("botncop");

//            executor.submit(matchHandler);
//            executor.submit(restockHandler);
//            executor.submit(rateLimiter);
            executor.submit(bncModule);

            try {
                while (true) {
                    lock.lock();
                    condition.await(1, TimeUnit.MINUTES);
                    lock.unlock();
                }
            } finally {
                lock.unlock();
            }
        } catch (Exception e) {
            log.error("{} caught in run method: {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }
}
