package org.secomm.pitwitter;

import org.secomm.pitwitter.handlers.TwitterHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication
public class PiTwitter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PiTwitter.class);

    @Autowired
    private TwitterHandler twitterHandler;

    public static void main(String... args) {
        SpringApplication.run(PiTwitter.class);
    }

    @Override
    public void run(String... args) {

        Lock lock;

        Condition condition;

        lock = new ReentrantLock();
        condition = lock.newCondition();

        try {
            twitterHandler.initialize();
            try {
                while (true) {
                    twitterHandler.run();
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
