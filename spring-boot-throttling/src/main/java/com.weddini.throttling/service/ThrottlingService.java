package com.weddini.throttling.service;

import com.weddini.throttling.ThrottlingKey;


public interface ThrottlingService {

    boolean throttle(ThrottlingKey key, String evaluatedValue);

}
