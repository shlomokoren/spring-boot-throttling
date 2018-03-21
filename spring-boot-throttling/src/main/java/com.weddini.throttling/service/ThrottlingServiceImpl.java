package com.weddini.throttling.service;

import com.weddini.throttling.ThrottlingGauge;
import com.weddini.throttling.ThrottlingKey;
import com.weddini.throttling.cache.Cache;
import com.weddini.throttling.cache.CacheBuilder;
import com.weddini.throttling.cache.CacheLoader;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.concurrent.ExecutionException;


public class ThrottlingServiceImpl implements ThrottlingService {

    private final Log logger = LogFactory.getLog(getClass());

    private final Cache<ThrottlingKey, ThrottlingGauge> cache;
    private final CacheLoader<ThrottlingKey, ThrottlingGauge> gaugeLoader = key -> new ThrottlingGauge(key.getTimeUnit(), key.getLimit());


    public ThrottlingServiceImpl(int cacheSize) {
        this.cache = CacheBuilder.<ThrottlingKey, ThrottlingGauge>builder()
                .setMaximumWeight(cacheSize)
                .build();
    }

    @Override
    public boolean throttle(ThrottlingKey key, String evaluatedValue) {

        try {

            ThrottlingGauge gauge = cache.computeIfAbsent(key, gaugeLoader);
            gauge.removeEldest();
            return gauge.throttle();

        } catch (ExecutionException e) {
            if (logger.isErrorEnabled()) {
                logger.error("exception occurred while calculating throttle value", e);
            }
        }

        return true;
    }

}
