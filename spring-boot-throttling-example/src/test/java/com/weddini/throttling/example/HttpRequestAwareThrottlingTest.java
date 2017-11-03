package com.weddini.throttling.example;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.Charset;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@WebAppConfiguration
public class HttpRequestAwareThrottlingTest {

    private final MediaType contentType = new MediaType(
            MediaType.APPLICATION_JSON.getType(),
            MediaType.APPLICATION_JSON.getSubtype(),
            Charset.forName("utf8")
    );

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;


    @Before
    public void setup() throws Exception {
        this.mockMvc = webAppContextSetup(webApplicationContext).build();
    }


    @Test
    public void testRemoteAddr() throws Exception {

        RequestPostProcessor postProcessor1 = request -> {
            request.setRemoteAddr("192.168.0.1");
            return request;
        };
        RequestPostProcessor postProcessor2 = request -> {
            request.setRemoteAddr("192.168.0.2");
            return request;
        };

        // remoteAddr = 192.168.0.1
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/throttling/remoteAddr/Alex")
                    .with(postProcessor1))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }
        mockMvc.perform(get("/throttling/remoteAddr/Alex")
                .with(postProcessor1))
                .andExpect(status().is(429));

        // remoteAddr = 192.168.0.2
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/throttling/remoteAddr/Vasya")
                    .with(postProcessor2))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }

        mockMvc.perform(get("/throttling/remoteAddr/Vasya")
                .with(postProcessor2))
                .andExpect(status().is(429));

        // sleep 1 minute
        Thread.sleep(60 * 1000 + 100);

        // remoteAddr = 192.168.0.1
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/throttling/remoteAddr/Alex")
                    .with(postProcessor1))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }
        mockMvc.perform(get("/throttling/remoteAddr/Alex")
                .with(postProcessor1))
                .andExpect(status().is(429));

        // remoteAddr = 192.168.0.2
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(get("/throttling/remoteAddr/Vasya")
                    .with(postProcessor2))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }

        mockMvc.perform(get("/throttling/remoteAddr/Vasya")
                .with(postProcessor2))
                .andExpect(status().is(429));
    }


    @Test
    public void testHeader() throws Exception {
        // remoteAddr = 10.10.10.10
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/throttling/header/Alex")
                    .header("X-Forwarded-For", "10.10.10.10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }
        mockMvc.perform(get("/throttling/header/Alex")
                .header("X-Forwarded-For", "10.10.10.10"))
                .andExpect(status().is(429));

        // remoteAddr = 10.10.10.101
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/throttling/header/Vasya")
                    .header("X-Forwarded-For", "10.10.10.101"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }

        mockMvc.perform(get("/throttling/header/Vasya")
                .header("X-Forwarded-For", "10.10.10.101"))
                .andExpect(status().is(429));

        // sleep 1 minute
        Thread.sleep(60 * 1000 + 100);

        // remoteAddr = 10.10.10.10
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/throttling/header/Alex")
                    .header("X-Forwarded-For", "10.10.10.10"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }
        mockMvc.perform(get("/throttling/header/Alex")
                .header("X-Forwarded-For", "10.10.10.10"))
                .andExpect(status().is(429));

        // remoteAddr = 10.10.10.101
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/throttling/header/Vasya")
                    .header("X-Forwarded-For", "10.10.10.101"))
                    .andExpect(status().isOk())
                    .andExpect(content().contentType(contentType));

        }

        mockMvc.perform(get("/throttling/header/Vasya")
                .header("X-Forwarded-For", "10.10.10.101"))
                .andExpect(status().is(429));

    }
}
