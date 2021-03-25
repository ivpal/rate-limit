package com.github.ivpal;

import javax.cache.expiry.Duration;

public class RateLimitRule {
    private final long count;
    private final Duration duration;

    private RateLimitRule(long count, Duration duration) {
        this.duration = duration;
        this.count = count;
    }

    public static RateLimitRule of(long count, Duration duration) {
        return new RateLimitRule(count, duration);
    }

    public long getCount() {
        return count;
    }

    public Duration getDuration() {
        return duration;
    }
}
