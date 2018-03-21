package com.weddini.throttling.cache;

import java.util.ArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class ReleasableLockPool {

    private final ArrayList<Tuple<ReleasableLock, ReleasableLock>> locks;

    public ReleasableLockPool(int poolSize) {
        locks = new ArrayList<>(poolSize);
        for (int i = 0; i < poolSize; i++) {
            ReadWriteLock lock = new ReentrantReadWriteLock();
            ReleasableLock readLock = new ReleasableLock(lock.readLock());
            ReleasableLock writeLock = new ReleasableLock(lock.writeLock());
            locks.add(i, Tuple.tuple(readLock, writeLock));
        }
    }

    public ReleasableLock getReadLockFor(Object resource) {
        return locks.get(resourceToIndex(resource)).v1();
    }

    public ReleasableLock getWriteLockFor(Object resource) {
        return locks.get(resourceToIndex(resource)).v2();
    }

    private int resourceToIndex(Object resource) {
        int hashCode = resource.hashCode();
        return Math.abs(hashCode) % locks.size();
    }

}
