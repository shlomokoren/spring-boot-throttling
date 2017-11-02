package com.weddini.throttling.example;

import com.weddini.throttling.ThrottlingException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.Assert;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DummyServiceTest {

    @Autowired
    private Service service;

    @Test
    public void compute() throws Exception {
        Model model1 = Model.builder().userName("Misha").build();
        Model model2 = Model.builder().userName("Vasya").build();

        service.compute(model1);
        service.compute(model1);
        service.compute(model1);
        try {
            service.compute(model1);
        } catch (RuntimeException e) {
            Assert.isTrue(e instanceof ThrottlingException, "ThrottlingException should be thrown!");
        }

        service.compute(model2);
        service.compute(model2);
        service.compute(model2);
        try {
            service.compute(model2);
        } catch (RuntimeException e) {
            Assert.isTrue(e instanceof ThrottlingException, "ThrottlingException should be thrown!");
        }

        Thread.sleep(60 * 1000 + 10);

        service.compute(model1);
        service.compute(model1);
        service.compute(model1);
        try {
            service.compute(model1);
        } catch (RuntimeException e) {
            Assert.isTrue(e instanceof ThrottlingException, "ThrottlingException should be thrown!");
        }

        service.compute(model2);
        service.compute(model2);
        service.compute(model2);
        try {
            service.compute(model2);
        } catch (RuntimeException e) {
            Assert.isTrue(e instanceof ThrottlingException, "ThrottlingException should be thrown!");
        }

    }

}