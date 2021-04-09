package com.github.ivpal;

import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.expiry.Duration;
import java.util.concurrent.TimeUnit;

public class BootstrapVerticle extends AbstractVerticle {
    private static final Logger logger = LoggerFactory.getLogger(BootstrapVerticle.class);

    private final Ignite ignite;

    public BootstrapVerticle(Ignite ignite) {
        this.ignite = ignite;
    }

    @Override
    public void start(Promise<Void> startPromise) {
        var storeOptions = new ConfigStoreOptions().setType("env");
        var retrieverOptions = new ConfigRetrieverOptions().addStore(storeOptions);
        var configRetriever = ConfigRetriever.create(vertx, retrieverOptions);
        configRetriever.getConfig()
            .flatMap(json -> {
                var limit = json.getLong("RL_LIMIT");
                if (limit == null) {
                    limit = 100L;
                }
                var unit = json.getString("RL_UNIT");
                if (unit == null) {
                    unit = "MINUTES";
                }
                var durationConfig = json.getLong("RL_DURATION");
                if (durationConfig == null) {
                    durationConfig = 1L;
                }
                var duration = new Duration(TimeUnit.valueOf(unit), durationConfig);
                var rateLimiter = new RateLimiter(vertx, ignite, new RateLimitRule(limit, duration));
                return vertx.deployVerticle(new ServerVerticle(rateLimiter));
            })
            .onSuccess(id -> {
                logger.info("Started");
                startPromise.complete();
            });
    }
}
