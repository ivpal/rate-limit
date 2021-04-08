package com.github.ivpal;

public class CacheItem {
    private long limit;
    private long count;
    private long expiredAfter;

    public CacheItem() {
    }

    public CacheItem(long limit, long count, long expiredAfter) {
        this.limit = limit;
        this.count = count;
        this.expiredAfter = expiredAfter;
    }

    public long getLimit() {
        return limit;
    }

    public void setLimit(long limit) {
        this.limit = limit;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public void increment() {
        count++;
    }

    public long getExpiredAfter() {
        return expiredAfter;
    }

    public void setExpiredAfter(long expiredAfter) {
        this.expiredAfter = expiredAfter;
    }
}
