package com.weddini.throttling.example;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/throttling")
public class DemoController {

    private final DemoService demoService;

    @Autowired
    public DemoController(DemoService demoService) {
        this.demoService = demoService;
    }

    @GetMapping("/SpEl/{userName}")
    public ResponseEntity<Model> spEl(@PathVariable(value = "userName") String userName) {
        return ResponseEntity.ok(demoService.computeWithSpElThrottling(Model.builder()
                .userName(userName)
                .build()));
    }

    @GetMapping("/header/{userName}")
    public ResponseEntity<Model> header(@PathVariable(value = "userName") String userName) {
        return ResponseEntity.ok(demoService.computeWithHttpHeaderThrottling(Model.builder()
                .userName(userName)
                .build()));
    }

    @GetMapping("/remoteAddr/{userName}")
    public ResponseEntity<Model> remoteAddr(@PathVariable(value = "userName") String userName) {
        return ResponseEntity.ok(demoService.computeWithHttpRemoteAddrThrottling(Model.builder()
                .userName(userName)
                .build()));
    }
}
