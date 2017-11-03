package com.weddini.throttling.example;

public interface DemoService {
    Model computeWithSpElThrottling(Model model);
    Model computeWithHttpHeaderThrottling(Model model);
    Model computeWithHttpRemoteAddrThrottling(Model model);
}
