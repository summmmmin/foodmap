package com.example.foodmap.user;

import com.example.foodmap.common.web.GlobalExceptionHandler;
import com.example.foodmap.user.controller.UserController;
import com.example.foodmap.user.dto.UserResponse;
import com.example.foodmap.user.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class)
@Import(GlobalExceptionHandler.class)
public class UserControllerTest {
    @Autowired MockMvc mvc;
    @MockitoBean
    UserService userService;

    @Test
    void getUserById_returns200() throws Exception {
        UserResponse resp = new UserResponse(
                1L, "id", "이름", "Y",
                LocalDateTime.now(), LocalDateTime.now()
        );
        Mockito.when(userService.getById(1L)).thenReturn(resp);

        mvc.perform(get("/api/users/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.loginId").value("id"))
                .andExpect(jsonPath("$.username").value("이름"))
                .andExpect(jsonPath("$.useYn").value("Y"));
    }

    @Test
    void getUserById_whenNotFound_returns404() throws Exception {
        Mockito.when(userService.getById(anyLong()))
                .thenThrow(new NoSuchElementException("User not found: 999"));

        mvc.perform(get("/api/users/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("User not found: 999"));
    }
}
