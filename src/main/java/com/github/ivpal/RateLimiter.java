package com.github.ivpal;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.expiry.CreatedExpiryPolicy;
import java.util.concurrent.TimeUnit;

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

    public Future<RateLimitResult> check(String token) {
        return vertx.executeBlocking(promise -> {
            IgniteCache<String, CacheItem> cache = ignite.getOrCreateCache(cacheName);
            cache = cache.withExpiryPolicy(new CreatedExpiryPolicy(rule.getDuration()));
            var result = new RateLimitResult();
            result.setLimit(rule.getLimit());
            result.setRemaining(0);

            var transactions = ignite.transactions();
            try (var tx = transactions.txStart()) {
                var item = cache.get(token);
                if (item == null) {
                    var duration = rule.getDuration();
                    var durationInMillis = duration.getTimeUnit().toMillis(duration.getDurationAmount());
                    item = new CacheItem(rule.getLimit(), 0L, System.currentTimeMillis() + durationInMillis);
                }

                if (item.getCount() < rule.getLimit() - 1) {
                    item.increment();
                    cache.put(token, item);
                    result.setRemaining(rule.getLimit() - item.getCount());
                    tx.commit();
                    result.setSuccess(true);
                    promise.complete(result);
                    return;
                } else {
                    var seconds = TimeUnit.MILLISECONDS.toSeconds(item.getExpiredAfter() - System.currentTimeMillis());
                    result.setRetryAfter(seconds);
                }
            }

            result.setSuccess(false);
            promise.complete(result);
        });
    }
}
