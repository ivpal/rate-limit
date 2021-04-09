package com.github.ivpal;

import javax.cache.expiry.Duration;

public class RateLimitRule {
    private final long limit;
    private final Duration duration;

    public RateLimitRule(long limit, Duration duration) {
        this.duration = duration;
        this.limit = limit;
    }

    public long getLimit() {
        return limit;
    }

    public Duration getDuration() {
        return duration;
    }
}
