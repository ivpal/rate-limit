package com.github.ivpal;

public class RateLimitResult {
    private boolean success;
    private long remaining;
    private long limit;
    private long retryAfter;

    public RateLimitResult() {
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public long getRemaining() {
        return remaining;
    }

    public void setRemaining(long remaining) {
        this.remaining = remaining;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getRetryAfter() {
        return retryAfter;
    }

    public void setRetryAfter(long retryAfter) {
        this.retryAfter = retryAfter;
    }

    @Override
    public String toString() {
        return "RateLimitResult{" +
            "success=" + success +
            ", remaining=" + remaining +
            ", limit=" + limit +
            ", retryAfter=" + retryAfter +
            '}';
    }
}
