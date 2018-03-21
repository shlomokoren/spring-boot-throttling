package com.weddini.throttling;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Class holding method calls information
 * Used as a value in {@link com.weddini.throttling.cache.Cache}
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public class ThrottlingGauge {

    private final int throttleLimit;
    private final long mills;
    private final ArrayList<Long> callTimestamps;
    private final ReadWriteLock lock;

    public ThrottlingGauge(TimeUnit timeUnit, int throttleLimit) {
        this.throttleLimit = throttleLimit;
        mills = timeUnit.toMillis(1);
        callTimestamps = new ArrayList<>();
        lock = new ReentrantReadWriteLock(true);
    }

    public boolean throttle() {
        lock.readLock().lock();
        try {
            if (callTimestamps.size() >= throttleLimit) {
                return false;
            }
        } finally {
            lock.readLock().unlock();
        }

        lock.writeLock().lock();
        try {
            if (callTimestamps.size() < throttleLimit) {
                callTimestamps.add(System.currentTimeMillis());
                return true;
            } else {
                return false;
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeEldest() {
        long threshold = System.currentTimeMillis() - this.mills;
        lock.writeLock().lock();
        try {
            callTimestamps.removeIf(it -> it < threshold);
        } finally {
            lock.writeLock().unlock();
        }
    }
}
