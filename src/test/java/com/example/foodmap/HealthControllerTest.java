package com.example.foodmap;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = HealthController.class)
public class HealthControllerTest {
    @Autowired
    MockMvc mvc;

    @Test
    void health_returns_ok() throws Exception {
        mvc.perform(get("/api/health")).andExpect(status().isOk()).andExpect(content().string("ok"));
    }
}
