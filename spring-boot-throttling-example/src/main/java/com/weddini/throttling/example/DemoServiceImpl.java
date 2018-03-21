package com.weddini.throttling.example;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DemoServiceImpl implements DemoService {

    /**
     * Throttling configuration:
     * <p>
     * allow 3 method calls per minute
     * for each userName in model object
     * passed as parameter
     */
    @Override
    @Throttling(limit = 3,
            timeUnit = TimeUnit.MINUTES,
            type = ThrottlingType.SpEL,
            expression = "#model.userName")
    public Model computeWithSpElThrottling(Model model) {
        log.info("computeWithSpElThrottling..., userName = {}", model.getUserName());
        return model;
    }

    /**
     * Throttling configuration:
     * <p>
     * allow 10 method calls per minute
     * for each unique {@code javax.servlet.http.HttpServletRequest#getHeader()}
     */
    @Override
    @Throttling(limit = 10,
            timeUnit = TimeUnit.MINUTES,
            type = ThrottlingType.HeaderValue,
            headerName = "X-Forwarded-For")
    public Model computeWithHttpHeaderThrottling(Model model) {
        log.info("computeWithHttpHeaderThrottling..., userName = {}", model.getUserName());
        return model;
    }

    /**
     * Throttling configuration:
     * <p>
     * allow 5 method calls per minute
     * for each unique {@code javax.servlet.http.HttpServletRequest#getRemoteAddr()}
     */
    @Override
    @Throttling(limit = 5, timeUnit = TimeUnit.MINUTES)
    public Model computeWithHttpRemoteAddrThrottling(Model model) {
        log.info("computeWithHttpRemoteAddrThrottling..., userName = {}", model.getUserName());
        return model;
    }
}
