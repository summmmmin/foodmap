package com.example.foodmap.external.kakao;

import com.example.foodmap.place.dto.Place;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class KakaoLocalClientTest {

    static MockWebServer server;

    @BeforeAll
    static void start() throws IOException {
        server = new MockWebServer();
        server.start();
    }

    @AfterAll
    static void stop() throws IOException {
        server.shutdown();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        String baseUrl = server.url("/").toString().replaceAll("/$", "");
        registry.add("kakao.api.base-url", () -> baseUrl);
        registry.add("kakao.api.key", () -> "dummy");     // 헤더 검증에 사용
        registry.add("kakao.api.timeout-ms", () -> "1000");
    }

    @Autowired
    KakaoLocalClient client;

    @Test
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

        var result = client.geocodeAddress("서울 강남구 테헤란로 123");
        assertThat(result).isPresent();
        double[] latlon = result.get();
        assertThat(latlon[0]).isEqualTo(37.498);   // lat
        assertThat(latlon[1]).isEqualTo(127.027);  // lon

        // 헤더(Authorization)까지 들어갔는지 확인
        var recorded = server.takeRequest();
        assertThat(recorded.getPath()).startsWith("/v2/local/search/address.json");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("KakaoAK dummy");
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
        assertThat(list).hasSize(1);
        Place p = list.get(0);
        assertThat(p.kakaoPlaceId()).isEqualTo("K1");
        assertThat(p.name()).isEqualTo("김밥천국");
        assertThat(p.categoryGroupCode()).isEqualTo("FD6");
        assertThat(p.latitude()).isEqualTo(37.501);
        assertThat(p.longitude()).isEqualTo(127.001);

        var recorded = server.takeRequest();
        assertThat(recorded.getPath()).startsWith("/v2/local/search/category.json");
        assertThat(recorded.getHeader("Authorization")).isEqualTo("KakaoAK dummy");
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
        assertThat(result).isEmpty();
    }
}
