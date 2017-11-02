package com.weddini.throttling.example;

import com.weddini.throttling.Throttling;
import com.weddini.throttling.ThrottlingType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class DummyService implements com.weddini.throttling.example.Service {

    /**
     * Throttling configuration:
     *
     *      allow 3 method calls per minute
     *      for each userName in model object
     *      passed as parameter
     */
    @Override
    @Throttling(limit = 3,
            timeUnit = TimeUnit.MINUTES,
            type = ThrottlingType.SpEL,
            expression = "#model.userName")
    public void compute(Model model) {
        log.info("computing..., userName = {}", model.getUserName());
    }
}
