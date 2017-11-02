package com.weddini.throttling;

import org.junit.Test;
import org.springframework.util.Assert;

import java.util.concurrent.TimeUnit;

public class ThrottlingGaugeTest {

    @Test
    public void testThrottlingGauge() throws InterruptedException {
        ThrottlingGauge gauge = new ThrottlingGauge(TimeUnit.SECONDS, 1);

        gauge.removeEldest();
        Assert.isTrue(gauge.throttle(), "Should be ok with the first call");

        gauge.removeEldest();
        Assert.isTrue(!gauge.throttle(), "Shouldn't be ok with the next call");

        gauge.removeEldest();
        Assert.isTrue(!gauge.throttle(), "Shouldn't be ok with the next call");

        gauge.removeEldest();
        Assert.isTrue(!gauge.throttle(), "Shouldn't be ok with the next call");

        Thread.sleep(1100);

        gauge.removeEldest();
        Assert.isTrue(gauge.throttle(), "Should be ok with the call after sleep 1 sec.");
    }

}