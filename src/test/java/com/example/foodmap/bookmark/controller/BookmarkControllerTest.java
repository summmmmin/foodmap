package com.example.foodmap.bookmark.controller;

import com.example.foodmap.bookmark.domain.BookmarkEntity;
import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.bookmark.service.BookmarkService;
import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.place.dto.BookmarkAddRequest;
import com.example.foodmap.place.dto.Place;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.annotation.Resource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = BookmarkController.class)
class BookmarkControllerTest extends MySqlContainerConfig {

    @Resource
    MockMvc mockMvc;

    @Resource
    ObjectMapper objectMapper;

    @MockitoBean
    BookmarkService bookmarkService;

    @Test
    void addFromExternal_shouldCallService() throws Exception {
        var place = new Place("kakao-1","상호","FD6","음식점","한식","도로",37.0,127.0);
        var body = new BookmarkAddRequest(place, "메모");

        mockMvc.perform(post("/api/bookmarks/external")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("X-USER-ID", "1")
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(bookmarkService).addFromExternal(1L, place, "메모");
    }

    @Test
    void addByPlaceId_shouldCallService() throws Exception {
        mockMvc.perform(post("/api/bookmarks/10")
                        .param("memo", "주말")
                        .header("X-USER-ID", "1"))
                .andExpect(status().isOk());

        verify(bookmarkService).addByPlaceId(1L, 10L, "주말");
    }

    @Test
    void getOne_shouldReturnView() throws Exception {
        var view = new BookmarkView(5L, "m", 10L, "kakao-1", "상호",
                "FD6", "음식점", "한식", "도로", 37.0, 127.0, "2025-08-30 12:00:00");
        when(bookmarkService.getBookmarkView(5L)).thenReturn(view);

        mockMvc.perform(get("/api/bookmarks/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookmarkId").value(5))
                .andExpect(jsonPath("$.kakaoPlaceId").value("kakao-1"));
    }

    @Test
    void listByUser_shouldReturnArray() throws Exception {
        when(bookmarkService.listByUserId(1L))
                .thenReturn(List.of(mock(BookmarkView.class)));

        mockMvc.perform(get("/api/bookmarks").header("X-USER-ID", "1"))
                .andExpect(status().isOk());
        verify(bookmarkService).listByUserId(1L);
    }

    @Test
    void deleteByUserAndPlace_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/bookmarks/by-place/10").header("X-USER-ID","1"))
                .andExpect(status().isNoContent());
        verify(bookmarkService).deleteByUserAndPlace(1L, 10L);
    }

    @Test
    void deleteById_shouldCallService() throws Exception {
        mockMvc.perform(delete("/api/bookmarks/5"))
                .andExpect(status().isNoContent());
        verify(bookmarkService).deleteByBookmarkId(5L);
    }
}
