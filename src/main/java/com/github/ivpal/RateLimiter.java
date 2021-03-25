package com.github.ivpal;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.configuration.CacheConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.cache.expiry.CreatedExpiryPolicy;
import java.util.HashMap;
import java.util.Map;

public class RateLimiter {
    private final Logger logger = LoggerFactory.getLogger(RateLimiter.class);

    private final Vertx vertx;
    private final Ignite ignite;
    private final Map<String, RateLimitRule> rules;

    private RateLimiter(Vertx vertx, Ignite ignite, Map<String, RateLimitRule> rules) {
        this.vertx = vertx;
        this.ignite = ignite;
        this.rules = rules;
    }

    public Future<Boolean> check(String path) {
        return vertx.executeBlocking(promise -> {
            var rule = rules.get(path);
            if (rule == null) {
                promise.complete(true);
                return;
            }

            IgniteCache<String, Long> cache = ignite.cache(path);
            var transactions = ignite.transactions();
            try (var tx = transactions.txStart()) {
                var count = cache.get(path);
                if (count == null) {
                    count = 0L;
                }

                if (count < rule.getCount() - 1) {
                    cache.put(path, ++count);
                    tx.commit();
                    promise.complete(true);
                    return;
                }
            }

            promise.complete(false);
        });
    }

    private Future<Boolean> initialize() {
        return vertx.executeBlocking(promise -> {
            rules.forEach((path, rule) -> {
                var cfg = new CacheConfiguration<String, Long>();
                cfg.setName(path);
                cfg.setExpiryPolicyFactory(CreatedExpiryPolicy.factoryOf(rule.getDuration()));
                ignite.createCache(cfg);
            });
            promise.complete(true);
        });
    }

    public static class Builder {
        private Vertx vertx;
        private Ignite ignite;
        private final Map<String, RateLimitRule> rules = new HashMap<>();

        public Builder setVertx(Vertx vertx) {
            this.vertx = vertx;
            return this;
        }

        public Builder setIgnite(Ignite ignite) {
            this.ignite = ignite;
            return this;
        }

        public Builder addRule(String path, RateLimitRule rule) {
            rules.put(path, rule);
            return this;
        }

        public Future<RateLimiter> build() {
            if (vertx == null) {
                throw new RuntimeException("You must set Vert.x instance via setVertx(...)");
            }

            if (ignite == null) {
                throw new RuntimeException("You must set Ignite instance via setIgnite(...)");
            }

            var rateLimiter = new RateLimiter(vertx, ignite, rules);
            return rateLimiter.initialize().map(result -> rateLimiter);
        }
    }
}
