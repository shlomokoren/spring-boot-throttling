package com.weddini.throttling;


import org.junit.Test;
import org.springframework.util.Assert;

public class LRUCacheTest {

    @Test
    public void testLRUCache() {
        LRUCache<Integer, Integer> cache = new LRUCache<>(3);
        cache.put(1, 1);
        cache.put(2, 1);
        cache.put(3, 1);

        Assert.isTrue(3 == cache.size(), "Size should be equal 3");

        cache.put(4, 1);

        Assert.isTrue(!cache.containsKey(1), "Entry with key = 1 should be evicted");
        Assert.isTrue(3 == cache.size(), "Size should be equal 3");

        cache.put(5, 1);

        Assert.isTrue(!cache.containsKey(2), "Entry with key = 2 should be evicted");
        Assert.isTrue(3 == cache.size(), "Size should be equal 3");

        cache.put(6, 1);

        Assert.isTrue(!cache.containsKey(3), "Entry with key = 3 should be evicted");
        Assert.isTrue(3 == cache.size(), "Size should be equal 3");

        cache.get(4); // access tha latest added entry
        cache.put(7, 1);

        Assert.isTrue(!cache.containsKey(5), "Entry with key = 5 should be evicted");
        Assert.isTrue(3 == cache.size(), "Size should be equal 3");
        Assert.isTrue(cache.containsKey(4), "Entry with key = 4 should be in cache");
        Assert.isTrue(cache.containsKey(6), "Entry with key = 6 should be in cache");
        Assert.isTrue(cache.containsKey(7), "Entry with key = 7 should be in cache");
    }
}