package com.weddini.throttling;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static com.weddini.throttling.ThrottlingType.SpEL;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * {@link org.springframework.beans.factory.config.BeanPostProcessor}
 * implementation that facilitates {@link Throttling} configuration
 * and decides whether a method invocation is allowed or not.
 * In case method reaches {@link Throttling} configuration limit
 * {@link ThrottlingException} is thrown.
 *
 * @author Nikolay Papakha (nikolay.papakha@gmail.com)
 */
public class ThrottlingBeanPostProcessor implements BeanPostProcessor {

    @Resource
    private HttpServletRequest servletRequest;

    private final LRUCache<ThrottlingKey, ThrottlingGauge> cache;
    private final Map<String, Class> beanNamesToOriginalClasses;
    private final SpElEvaluator spelEvaluator;
    private final ReadWriteLock lock;

    public ThrottlingBeanPostProcessor(int cacheCapacity) {
        cache = new LRUCache<>(cacheCapacity);
        beanNamesToOriginalClasses = new HashMap<>();
        spelEvaluator = new SpElEvaluator();
        lock = new ReentrantReadWriteLock(true);
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        for (Method method : bean.getClass().getMethods()) {
            Throttling annotation = method.getAnnotation(Throttling.class);
            if (annotation != null) {
                beanNamesToOriginalClasses.put(beanName, bean.getClass());
                break;
            }
        }
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> clazz = beanNamesToOriginalClasses.get(beanName);
        if (clazz == null) {
            return bean;
        }

        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (proxy, method, args) -> {
            Throttling annotation = findAnnotation(clazz.getMethod(method.getName(), method.getParameterTypes()), Throttling.class);
            if (annotation != null) {
                ThrottlingKey key = ThrottlingKey.builder()
                        .method(method)
                        .throttling(annotation)
                        .headerValue(resolveRequestValue(annotation, ThrottlingType.HeaderValue))
                        .cookieValue(resolveRequestValue(annotation, ThrottlingType.CookieValue))
                        .principal(resolveRequestValue(annotation, ThrottlingType.PrincipalName))
                        .expression(resolveSpEl(annotation, bean, args, clazz, method))
                        .build();

                ThrottlingGauge gauge;
                // read the gauge
                lock.readLock().lock();
                try {
                    gauge = cache.get(key);
                } finally {
                    lock.readLock().unlock();
                }

                if (gauge == null) {
                    // write the gauge
                    lock.writeLock().lock();
                    try {
                        if (gauge == null) {
                            gauge = new ThrottlingGauge(key.getTimeUnit(), key.getLimit());
                            cache.put(key, gauge);
                        }
                    } finally {
                        lock.writeLock().unlock();
                    }
                }

                gauge.removeEldest();

                if (!gauge.throttle()) throw new ThrottlingException();
            }

            // call original method
            return ReflectionUtils.invokeMethod(method, bean, args);
        });
    }

    private String resolveSpEl(Throttling annotation, Object bean, Object[] args, Class<?> clazz, Method method) {
        String value = null;
        if (annotation.type().equals(SpEL) && !StringUtils.isEmpty(annotation.expression())) {
            value = spelEvaluator.evaluate(annotation.expression(), bean, args, clazz, method);
        }
        return value;
    }

    private String resolveRequestValue(Throttling annotation, ThrottlingType targetValueType) {
        String value = null;

        if (annotation.type().equals(targetValueType) && servletRequest != null) {

            switch (targetValueType) {
                case CookieValue:
                    if (!StringUtils.isEmpty(annotation.cookieName())) {
                        value = Arrays.stream(servletRequest.getCookies())
                                .filter(c -> c.getName().equals(annotation.cookieName()))
                                .findFirst()
                                .map(Cookie::getValue)
                                .orElse(null);
                    }
                    break;

                case HeaderValue:
                    if (!StringUtils.isEmpty(annotation.headerName())) {
                        value = servletRequest.getHeader(annotation.headerName());
                    }
                    break;

                case PrincipalName:
                    if (servletRequest.getUserPrincipal() != null) {
                        value = servletRequest.getUserPrincipal().getName();
                    }
                    break;

                case RemoteAddr:
                    value = servletRequest.getRemoteAddr();
                    break;
            }
        }

        return value;
    }
}
