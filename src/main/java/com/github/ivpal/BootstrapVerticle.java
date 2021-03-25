package com.github.ivpal;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.expiry.Duration;

public class BootstrapVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BootstrapVerticle.class);

    private final Ignite ignite;

    public BootstrapVerticle(Ignite ignite) {
        this.ignite = ignite;
    }

    @Override
    public void start(Promise<Void> startPromise) throws Exception {
        new RateLimiter.Builder()
            .setVertx(vertx)
            .setIgnite(ignite)
            .addRule("/first", RateLimitRule.of(100, Duration.ONE_MINUTE))
            .addRule("/second", RateLimitRule.of(100, Duration.FIVE_MINUTES))
            .build()
            .compose(rateLimiter -> vertx.deployVerticle(new ServerVerticle(rateLimiter)))
            .onSuccess(id -> {
                logger.info("Started");
                startPromise.complete();
            });
    }
}
