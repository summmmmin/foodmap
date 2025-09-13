package com.example.foodmap.place;

import com.example.foodmap.common.error.BusinessException;
import com.example.foodmap.common.error.ErrorCode;
import com.example.foodmap.common.web.GlobalExceptionHandler;
import com.example.foodmap.place.controller.PlaceByLocationController;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.dto.PlaceSearchRequest;
import com.example.foodmap.place.service.PlaceSearchService;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = PlaceByLocationController.class)
@Import(GlobalExceptionHandler.class)
class PlaceByLocationControllerTest {

    @Autowired MockMvc mvc;

    @MockitoBean
    PlaceSearchService service;

    @Test
    void byLocation_withAddress_returnsList() throws Exception {
        Mockito.when(service.findByLocation(any(PlaceSearchRequest.class)))
                .thenReturn(List.of(new Place("K1","식당","FD6","음식점","한식","서울",37.5,127.0)));

        mvc.perform(get("/api/places/by-location")
                        .param("address", "서울 강남구 테헤란로 123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].kakaoPlaceId").value("K1"));
    }

    @Test
    void byLocation_withXY_returnsList() throws Exception {
        Mockito.when(service.findByLocation(any(PlaceSearchRequest.class)))
                .thenReturn(List.of(new Place("K2","김밥","FD6","음식점","분식","서울...",37.51,127.01)));

        mvc.perform(get("/api/places/by-location")
                        .param("x","127.0")
                        .param("y","37.5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].kakaoPlaceId").value("K2"));
    }

    @Test
    void byLocation_missingParams_returns400() throws Exception {
        Mockito.when(service.findByLocation(any(PlaceSearchRequest.class)))
                .thenThrow(new BusinessException(ErrorCode.INVALID_INPUT_VALUE, "주소 또는 좌표(x,y) 중 하나는 반드시 필요합니다."));

        mvc.perform(get("/api/places/by-location"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("E001"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/api/places/by-location"));
    }
}
