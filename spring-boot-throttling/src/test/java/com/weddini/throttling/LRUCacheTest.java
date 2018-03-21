package com.weddini.throttling;

import com.weddini.throttling.cache.Cache;
import com.weddini.throttling.cache.CacheBuilder;
import org.junit.Test;
import org.springframework.util.Assert;

public class LRUCacheTest {

    @Test
    public void testLRUCache() {


        Cache<Integer, Integer> cache = CacheBuilder.<Integer, Integer>builder().setMaximumWeight(3).build();
        cache.put(1, 1);
        cache.put(2, 1);
        cache.put(3, 1);

        Assert.isTrue(3 == cache.count(), "size should be equal 3");

        cache.put(4, 1);

        Assert.isNull(cache.get(1), "entry with key = 1 should be evicted");
        Assert.isTrue(3 == cache.count(), "size should be equal 3");

        cache.put(5, 1);

        Assert.isNull(cache.get(2), "entry with key = 2 should be evicted");
        Assert.isTrue(3 == cache.count(), "size should be equal 3");

        cache.put(6, 1);

        Assert.isNull(cache.get(3), "entry with key = 3 should be evicted");
        Assert.isTrue(3 == cache.count(), "size should be equal 3");

        cache.get(4); // access tha latest added entry
        cache.put(7, 1);

        Assert.isNull(cache.get(5), "entry with key = 5 should be evicted");
        Assert.isTrue(3 == cache.count(), "size should be equal 3");
        Assert.notNull(cache.get(4), "entry with key = 4 should be in cache");
        Assert.notNull(cache.get(6), "entry with key = 6 should be in cache");
        Assert.notNull(cache.get(7), "entry with key = 7 should be in cache");
        
    }
}
