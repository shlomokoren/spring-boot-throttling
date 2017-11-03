package com.weddini.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

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

    private final Log logger = LogFactory.getLog(getClass());

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
                if (logger.isDebugEnabled()) {
                    logger.debug("Discovered bean '" + beanName + "' annotated with @Throttling");
                }
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

        if (logger.isDebugEnabled()) {
            logger.debug("Replacing bean '" + beanName + "' with a Proxy");
        }

        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (proxy, method, args) -> {

            Throttling annotation = findAnnotation(clazz.getMethod(method.getName(), method.getParameterTypes()), Throttling.class);

            if (annotation != null) {

                ThrottlingValueEvaluator throttlingEvaluator = () -> {
                    String value = null;

                    if (annotation.type().equals(SpEL) && !StringUtils.isEmpty(annotation.expression())) {
                        try {
                            value = spelEvaluator.evaluate(annotation.expression(), bean, args, clazz, method);
                        } catch (Throwable t) {
                            logger.error("Exception occurred while evaluating SpEl expression = '" +
                                    annotation.expression() + "', Please check @Throttling configuration.", t);
                        }

                    } else {

                        HttpServletRequest servletRequest = null;
                        try {
                            servletRequest = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
                        } catch (IllegalStateException e) {
                            logger.error("No RequestAttributes object is bound to the current thread. " +
                                    "Please check @Throttling configuration.", e);
                        }
                        
                        if (servletRequest == null) {
                            logger.error("Cannot find HttpServletRequest in RequestContextHolder while processing " +
                                    "@Throttling annotation with type '" + annotation.type().name() + "'");

                        } else {

                            switch (annotation.type()) {
                                case CookieValue:
                                    if (!StringUtils.isEmpty(annotation.cookieName())) {
                                        value = Arrays.stream(servletRequest.getCookies())
                                                .filter(c -> c.getName().equals(annotation.cookieName()))
                                                .findFirst()
                                                .map(Cookie::getValue)
                                                .orElse(null);
                                    } else {
                                        logger.warn("Cannot resolve HTTP cookie value for empty cookie name. " +
                                                "Please check @Throttling configuration.");
                                    }
                                    break;

                                case HeaderValue:
                                    if (!StringUtils.isEmpty(annotation.headerName())) {
                                        value = servletRequest.getHeader(annotation.headerName());
                                    } else {
                                        logger.warn("Cannot resolve HTTP header value for empty header name. " +
                                                "Please check @Throttling configuration.");
                                    }
                                    break;

                                case PrincipalName:
                                    if (servletRequest.getUserPrincipal() != null) {
                                        value = servletRequest.getUserPrincipal().getName();
                                    } else {
                                        logger.warn("Cannot resolve servletRequest.getUserPrincipal().getName() " +
                                                "since servletRequest.getUserPrincipal() is null.");
                                    }
                                    break;

                                case RemoteAddr:
                                    value = servletRequest.getRemoteAddr();
                                    break;
                            }
                        }
                    }
                    
                    return value;
                };

                final String evaluatedValue = throttlingEvaluator.evaluate();

                ThrottlingKey key = ThrottlingKey.builder()
                        .method(method)
                        .annotation(annotation)
                        .evaluatedValue(evaluatedValue)
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

                if (!gauge.throttle()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Cannot proceed with a method call due to @Throttling configuration, type="
                                + annotation.type() + ", value=" + evaluatedValue);
                    }
                    throw new ThrottlingException();
                }

            }

            // call original method
            return ReflectionUtils.invokeMethod(method, bean, args);
        });
    }

    @FunctionalInterface
    public interface ThrottlingValueEvaluator {
        String evaluate();
    }

}
