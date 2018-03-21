package com.weddini.throttling.support;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingException;
import com.weddini.throttling.ThrottlingKey;
import com.weddini.throttling.service.ThrottlingEvaluator;
import com.weddini.throttling.service.ThrottlingService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Controller;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

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


    private final Map<String, Class> beanNamesToOriginalClasses;

    private final ThrottlingEvaluator throttlingEvaluator;
    private final ThrottlingService throttlingService;


    public ThrottlingBeanPostProcessor(ThrottlingEvaluator throttlingEvaluator, ThrottlingService throttlingService) {
        this.throttlingEvaluator = throttlingEvaluator;
        this.throttlingService = throttlingService;
        beanNamesToOriginalClasses = new HashMap<>();
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {

        for (Annotation annotation : bean.getClass().getAnnotations()) {
            // do not wrap sprint controllers with a proxy
            if (Controller.class.isInstance(annotation) || RestController.class.isInstance(annotation)) {
                return bean;
            }
        }

        for (Method method : bean.getClass().getMethods()) {
            Throttling annotation = method.getAnnotation(Throttling.class);
            if (annotation != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("discovered bean '" + beanName + "' annotated with @Throttling");
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
            logger.debug("replacing bean '" + beanName + "' with a proxy");
        }

        return Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), (proxy, method, args) -> {

            Throttling annotation = findAnnotation(clazz.getMethod(method.getName(), method.getParameterTypes()), Throttling.class);

            if (annotation != null) {


                final String evaluatedValue = throttlingEvaluator.evaluate(annotation, bean, clazz, method, args);

                ThrottlingKey key = ThrottlingKey.builder()
                        .method(method)
                        .annotation(annotation)
                        .evaluatedValue(evaluatedValue)
                        .build();

                boolean isAllowed = throttlingService.throttle(key, evaluatedValue);

                if (!isAllowed) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("cannot proceed with a method call due to @Throttling configuration, type="
                                + annotation.type() + ", value=" + evaluatedValue);
                    }
                    throw new ThrottlingException();
                }

            }

            // call original method
            return ReflectionUtils.invokeMethod(method, bean, args);
        });
    }

}
