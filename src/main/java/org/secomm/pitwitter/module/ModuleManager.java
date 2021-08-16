package org.secomm.pitwitter.module;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class ModuleManager {

    private final MatchModule matchModule;

    private final MentionsModule mentionsModule;

    private final RestocksModule restocksModule;

    private final CategoriesModule categoriesModule;

    private final FollowModule bncModule;

    private final RateLimiter rateLimiter;

    public ModuleManager(final MatchModule matchModule,
                         final MentionsModule mentionsModule,
                         final RestocksModule restocksModule,
                         final CategoriesModule categoriesModule,
                         final FollowModule bncModule,
                         final RateLimiter rateLimiter) {
        this.matchModule = matchModule;
        this.mentionsModule = mentionsModule;
        this.restocksModule = restocksModule;
        this.categoriesModule = categoriesModule;
        this.bncModule = bncModule;
        this.rateLimiter = rateLimiter;
    }

    public void startModules() throws Exception {

        matchModule.initialize();
        rateLimiter.register(matchModule);

        mentionsModule.initialize();

        restocksModule.initialize();
        rateLimiter.register(restocksModule);

        categoriesModule.initialize();
        rateLimiter.register(categoriesModule);
    }
}
