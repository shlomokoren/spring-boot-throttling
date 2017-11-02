package com.weddini.throttling.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "spring.throttling")
public class ThrottlingProperties {

    private Integer lruCacheCapacity;

    public ThrottlingProperties() {
    }

    public ThrottlingProperties(Integer lruCacheCapacity) {
        this.lruCacheCapacity = lruCacheCapacity;
    }

    public Integer getLruCacheCapacity() {
        return lruCacheCapacity;
    }

    public void setLruCacheCapacity(Integer lruCacheCapacity) {
        this.lruCacheCapacity = lruCacheCapacity;
    }
}
