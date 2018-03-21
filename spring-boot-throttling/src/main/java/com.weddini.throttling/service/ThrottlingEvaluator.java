package com.weddini.throttling.service;

import com.weddini.throttling.Throttling;

import java.lang.reflect.Method;


public interface ThrottlingEvaluator {

    String evaluate(Throttling throttlingConfig, Object bean, Class clazz, Method method, Object[] args);

}
