package org.secomm.pitwitter;

import org.secomm.pitwitter.database.MongoDbConnector;
import org.secomm.pitwitter.discord.DiscordAdapter;
import org.secomm.pitwitter.module.*;
import org.secomm.pitwitter.connectors.TwitterConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.context.annotation.PropertySource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@SpringBootApplication(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
@PropertySource("classpath:ssl.properties")
public class PiTwitter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(PiTwitter.class);

    @Value("${keystore.path}")
    private String keyStorePath;

    @Value("${keystore.password}")
    private String keyStorePassword;

    @Autowired
    private MongoDbConnector mongoDbConnector;

    @Autowired
    private TwitterConnector twitterConnector;

    @Autowired
    private RateLimiter rateLimiter;

    @Autowired
    private DiscordAdapter discordAdapter;

    @Autowired
    private ModuleManager moduleManager;

    public static void main(String... args) {
        SpringApplication.run(PiTwitter.class);
    }

    @Override
    public void run(String... args) {

/*
        System.setProperty("javax.net.ssl.trustStore", keyStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", keyStorePassword);
*/

        Lock lock;
        Condition condition;
        lock = new ReentrantLock();
        condition = lock.newCondition();
        boolean run = true;

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        try {
            mongoDbConnector.initialize();
            twitterConnector.initialize();

            log.info("Starting modules");
            moduleManager.startModules();

            executorService.submit(rateLimiter);

            try {
                while (run) {
                    lock.lock();
                    run = !condition.await(1, TimeUnit.MINUTES);
                    lock.unlock();
                }
            } finally {
                lock.unlock();
            }
            log.info("Twitter monitor ending");
        } catch (Exception e) {
            log.error("{} caught in run method: {}", e.getClass().getSimpleName(), e.getLocalizedMessage());
        }
    }
}
