package com.weddini.throttling.cache;


@FunctionalInterface
public interface CacheLoader<K, V> {

    V load(K key) throws Exception;

}
