package com.weddini.throttling.autoconfigure;

import com.weddini.throttling.ThrottlingBeanPostProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
        return new ThrottlingBeanPostProcessor(throttlingProperties.getLruCacheCapacity() != null ?
                throttlingProperties.getLruCacheCapacity() : DEFAULT_LRU_CACHE_CAPACITY);
    }
}
