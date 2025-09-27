package com.example.foodmap.place;

import com.example.foodmap.common.error.BusinessException;
import com.example.foodmap.common.error.ErrorCode;
import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.place.dto.PlaceSearchRequest;
import com.example.foodmap.place.service.GeocodeService;
import com.example.foodmap.place.service.NearbySearchService;
import com.example.foodmap.place.service.PlaceSearchService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
public class PlaceSearchServiceTest extends MySqlContainerConfig {

    NearbySearchService nearbySearchService;
    GeocodeService geocodeService;
    PlaceSearchService service;

    @BeforeEach
    void setUp() {
        geocodeService = mock(GeocodeService.class);
        nearbySearchService = mock(NearbySearchService.class);
        service = new PlaceSearchService(geocodeService, nearbySearchService);
    }

    @Test
    @DisplayName("주소 기반: 지오코딩 후 반경 검색 호출")
    void address_then_geocode_then_nearby() {
        // given
        var req = PlaceSearchRequest.builder()
                .address("서울 강남구 테헤란로 123")
                .radius(1500).size(10).page(1)
                .build();

        when(geocodeService.geocode(eq("서울 강남구 테헤란로 123")))
                .thenReturn(Optional.of(new double[]{37.498, 127.027}));
        var expected = List.of(new Place("K1","김밥천국","FD6","음식점","한식>분식","서울 강남구 ...",37.501,127.001));
        when(nearbySearchService.findNearby(eq("FD6"), anyDouble(), anyDouble(), eq(1500), eq(10), eq(1)))
                .thenReturn(expected);

        // when
        var result = service.findByLocation(req);

        // then
        assertEquals(1, result.size());
        assertEquals("K1", result.get(0).kakaoPlaceId());
        verify(geocodeService, times(1)).geocode("서울 강남구 테헤란로 123");
        verify(nearbySearchService, times(1))
                .findNearby(eq("FD6"), anyDouble(), anyDouble(), eq(1500), eq(10), eq(1));
    }

    @Test
    @DisplayName("주소 기반: 지오코딩 실패 시 빈 목록 반환")
    void address_geocode_empty_returns_empty() {
        var req = PlaceSearchRequest.builder()
                .address("없는 주소")
                .build();

        when(geocodeService.geocode(eq("없는 주소"))).thenReturn(Optional.empty());

        var result = service.findByLocation(req);

        assertTrue(result.isEmpty());
        verify(geocodeService).geocode("없는 주소");
        verifyNoInteractions(nearbySearchService);
    }

    @Test
    @DisplayName("좌표 기반: 위도 누락 예외")
    void coords_missingLatitude_throws() {
        var placeSearchRequest = PlaceSearchRequest.builder()
                .longitude(127.0)   // 위도 없음
                .build();

        assertThatThrownBy(() -> service.findByLocation(placeSearchRequest))
                .isInstanceOf(BusinessException.class)
                .satisfies(exception -> {
                    BusinessException businessException = (BusinessException) exception;
                    assertThat(businessException.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT_VALUE);
                })
                .hasMessageContaining("좌표");

        verifyNoInteractions(geocodeService, nearbySearchService);
    }

    @Test
    @DisplayName("좌표 기반: 주소 없이 좌표만 있으면 지오코딩 호출x")
    void coords_only_no_geocode() {
        var req = PlaceSearchRequest.builder()
                .longitude(127.0).latitude(37.5)
                .build();

        when(nearbySearchService.findNearby(eq("FD6"), eq(127.0), eq(37.5), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        service.findByLocation(req);

        verifyNoInteractions(geocodeService);
        verify(nearbySearchService).findNearby(eq("FD6"), eq(127.0), eq(37.5), anyInt(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("카테고리 공백이면 기본값 FD6 사용")
    void blank_category_defaults_to_fd6() {
        var req = PlaceSearchRequest.builder()
                .longitude(127.0).latitude(37.5)
                .categoryGroupCode("   ")
                .build();

        when(nearbySearchService.findNearby(anyString(), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt()))
                .thenReturn(List.of());

        service.findByLocation(req);

        verify(nearbySearchService).findNearby(eq("FD6"), anyDouble(), anyDouble(), anyInt(), anyInt(), anyInt());
    }
}
