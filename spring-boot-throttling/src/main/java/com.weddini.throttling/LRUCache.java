package com.weddini.throttling;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Simple LRU cache implementation based on {@link LinkedHashMap}
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public class LRUCache<K, V> extends LinkedHashMap<K, V> {

    private int maxSize;

    public LRUCache(int capacity, float loadFactor) {
        super(capacity, loadFactor, true);
        this.maxSize = capacity;
    }

    public LRUCache(int capacity) {
        this(capacity, 0.75f);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > maxSize;
    }
}
