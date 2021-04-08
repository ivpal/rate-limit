package com.github.ivpal;

import javax.cache.expiry.Duration;

public class RateLimitRule {
    private final long limit;
    private final Duration duration;

    private RateLimitRule(long limit, Duration duration) {
        this.duration = duration;
        this.limit = limit;
    }

    public static RateLimitRule of(long limit, Duration duration) {
        return new RateLimitRule(limit, duration);
    }

    public long getLimit() {
        return limit;
    }

    public Duration getDuration() {
        return duration;
    }
}
