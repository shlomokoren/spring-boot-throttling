package com.weddini.throttling;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;

import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;


public class ThrottlingKeyTest {

    static class A {
        @Throttling(limit = 5,
                timeUnit = TimeUnit.HOURS,
                type = ThrottlingType.HeaderValue,
                headerName = "X-Forwarded-For")
        public void testMethod() {
        }
    }

    static class B {
        @Throttling(limit = 3,
                timeUnit = TimeUnit.MINUTES,
                type = ThrottlingType.CookieValue,
                cookieName = "JSESSIONID")
        public void testMethod() {
        }
    }


    @Test
    public void testThrottlingKey() throws NoSuchMethodException {

        Method method1 = A.class.getMethod("testMethod");
        Method method2 = B.class.getMethod("testMethod");

        Throttling annotation1 = findAnnotation(method1, Throttling.class);
        Throttling annotation2 = findAnnotation(method1, Throttling.class);

        // method1 + annotation1 + "127.0.0.1"
        ThrottlingKey key1 = ThrottlingKey.builder()
                .method(method1)
                .annotation(annotation1)
                .evaluatedValue("127.0.0.1")
                .build();

        // method1 + annotation1 + "test"
        ThrottlingKey key4 = ThrottlingKey.builder()
                .method(method1)
                .annotation(annotation1)
                .evaluatedValue("test")
                .build();

        Assert.assertNotEquals(key1, key4);

        // method2 + annotation2 + "test"
        ThrottlingKey key2 = ThrottlingKey.builder()
                .method(method2)
                .annotation(annotation2)
                .evaluatedValue("test")
                .build();

        Assert.assertNotEquals(key1, key2);

        // method1 + annotation1 + "127.0.0.1"
        ThrottlingKey key3 = ThrottlingKey.builder()
                .method(method1)
                .annotation(annotation1)
                .evaluatedValue("127.0.0.1")
                .build();

        Assert.assertEquals(key1.hashCode(), key3.hashCode());
        Assert.assertEquals(key1, key3);

    }
}
