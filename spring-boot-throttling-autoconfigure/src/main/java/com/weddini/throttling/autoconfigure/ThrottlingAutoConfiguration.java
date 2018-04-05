package com.weddini.throttling.autoconfigure;

import com.weddini.throttling.service.ThrottlingEvaluator;
import com.weddini.throttling.service.ThrottlingEvaluatorImpl;
import com.weddini.throttling.service.ThrottlingService;
import com.weddini.throttling.service.ThrottlingServiceImpl;
import com.weddini.throttling.support.ThrottlingBeanPostProcessor;
import com.weddini.throttling.support.ThrottlingInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
@ConditionalOnClass(ThrottlingBeanPostProcessor.class)
@EnableConfigurationProperties(ThrottlingProperties.class)
public class ThrottlingAutoConfiguration {

    private static final int DEFAULT_LRU_CACHE_CAPACITY = 10000;

    private final ThrottlingProperties throttlingProperties;

    @Autowired
    public ThrottlingAutoConfiguration(ThrottlingProperties throttlingProperties) {
        this.throttlingProperties = throttlingProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingBeanPostProcessor throttlingBeanPostProcessor() {
        return new ThrottlingBeanPostProcessor(throttlingEvaluator(), throttlingService());
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication
    public ThrottlingInterceptor throttlingInterceptor() {
        return new ThrottlingInterceptor(throttlingEvaluator(), throttlingService());
    }

    @Bean
    @ConditionalOnWebApplication
    public WebMvcConfigurer interceptorAdapter() {
        return new WebMvcConfigurerAdapter() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(throttlingInterceptor());
            }
        };
    }


    @Bean
    @ConditionalOnMissingBean
    public ThrottlingEvaluator throttlingEvaluator() {
        return new ThrottlingEvaluatorImpl();
    }

    @Bean
    @ConditionalOnMissingBean
    public ThrottlingService throttlingService() {
        return new ThrottlingServiceImpl(throttlingProperties.getLruCacheCapacity() != null ?
                throttlingProperties.getLruCacheCapacity() : DEFAULT_LRU_CACHE_CAPACITY);
    }

}
