package com.github.ivpal;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.expiry.CreatedExpiryPolicy;

public class RateLimiter {
    private final Logger logger = LoggerFactory.getLogger(RateLimiter.class);
    private final String cacheName = "limits";

    private final Vertx vertx;
    private final Ignite ignite;
    private final RateLimitRule rule;

    public RateLimiter(Vertx vertx, Ignite ignite, RateLimitRule rule) {
        this.vertx = vertx;
        this.ignite = ignite;
        this.rule = rule;
    }

    public Future<Boolean> check(String token) {
        return vertx.executeBlocking(promise -> {
            IgniteCache<String, Long> cache = ignite.getOrCreateCache(cacheName);
            cache = cache.withExpiryPolicy(new CreatedExpiryPolicy(rule.getDuration()));

            var transactions = ignite.transactions();
            try (var tx = transactions.txStart()) {
                var count = cache.get(token);
                if (count == null) {
                    count = 0L;
                }

                if (count < rule.getCount() - 1) {
                    cache.put(token, ++count);
                    tx.commit();
                    promise.complete(true);
                    return;
                }
            }

            promise.complete(false);
        });
    }
}
