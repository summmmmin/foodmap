package com.example.foodmap.external.kakao;

import com.example.foodmap.config.MockServerConfiguration;
import com.example.foodmap.config.MySqlContainerConfig;
import com.example.foodmap.place.dto.Place;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Import(MockServerConfiguration.class)
class KakaoLocalClientTest extends MySqlContainerConfig {

    @Autowired
    KakaoLocalClient client;

    @Autowired
    MockWebServer server;

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry r) {
        String baseUrl = MockServerConfiguration.server().url("/").toString().replaceAll("/$", "");
        r.add("kakao.api.base-url", () -> baseUrl);
        r.add("kakao.api.key", () -> "dummy");
        r.add("kakao.api.timeout-ms", () -> "1000");
    }
    @Test
    @DisplayName("주소를 좌표로 변환")
    void geocodeAddress_returns_lat_lon() throws Exception {
        String body = """
        {
          "meta": { "total_count": 1 },
          "documents": [
            { "address_name": "서울 강남구 테헤란로 123", "x": "127.027", "y": "37.498" }
          ]
        }
        """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        Optional<double[]> result = client.geocodeAddress("서울 강남구 테헤란로 123");
        assertAll(
                () -> assertTrue(result.isPresent(), "좌표가 존재해야 한다"),
                () -> assertEquals(37.498, result.get()[0], 1e-9, "위도(lat)"),
                () -> assertEquals(127.027, result.get()[1], 1e-9, "경도(lon)")
        );

        // 헤더(Authorization)까지 들어갔는지 확인
        var recorded = server.takeRequest();
        assertNotNull(recorded, "요청이 수집 필요");
        assertTrue(recorded.getPath().startsWith("/v2/local/search/address.json"));
        assertEquals("KakaoAK dummy", recorded.getHeader("Authorization"));
    }

    @Test
    void searchCategory_returns_places() throws Exception {
        String body = """
        {
          "meta": { "total_count": 1, "pageable_count": 1, "is_end": true },
          "documents": [
            {
              "id": "K1",
              "place_name": "김밥천국",
              "category_group_code": "FD6",
              "category_group_name": "음식점",
              "category_name": "한식 > 분식",
              "road_address_name": "서울 강남구 ...",
              "address_name": "서울 강남구 ...",
              "x": "127.001",
              "y": "37.501"
            }
          ]
        }
        """;

        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(body));

        List<Place> list = client.searchCategory("FD6", 127.0, 37.5, 1500, 10, 1);
        assertAll(
                () -> assertNotNull(list),
                () -> assertEquals(1, list.size())
        );
        Place p = list.get(0);
        assertAll(
                () -> assertEquals("K1", p.kakaoPlaceId()),
                () -> assertEquals("김밥천국", p.name()),
                () -> assertEquals("FD6", p.categoryGroupCode()),
                () -> assertEquals(37.501, p.latitude(), 1e-9),
                () -> assertEquals(127.001, p.longitude(), 1e-9)
        );

        var recorded = server.takeRequest();
        assertTrue(recorded.getPath().startsWith("/v2/local/search/category.json"));
        assertEquals("KakaoAK dummy", recorded.getHeader("Authorization"));
    }

    @Test
    void geocodeAddress_when_empty_returns_emptyOptional() throws Exception {
        String empty = """
        { "meta": { "total_count": 0 }, "documents": [] }
        """;
        server.enqueue(new MockResponse()
                .setResponseCode(200)
                .setHeader("Content-Type", "application/json")
                .setBody(empty));

        var result = client.geocodeAddress("없는 주소");
        assertTrue(result.isEmpty());
    }
}
