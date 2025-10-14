package com.example.foodmap.bookmark;

import com.example.foodmap.bookmark.dto.BookmarkView;
import com.example.foodmap.place.dto.BookmarkAddRequest;
import com.example.foodmap.place.dto.Place;
import com.example.foodmap.config.MySqlContainerConfig;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BookmarkIntegrationTest extends MySqlContainerConfig{

    private static final String USER_ID_HEADER_NAME = "X-USER-ID";
    private static final String DEFAULT_USER_ID = "1";

    @LocalServerPort
    int port;

    private final RestTemplate restTemplate = new RestTemplate();

    private String buildUrl(String path) {
        return "http://localhost:" + port + path;
    }

    @Test
    void add_thenList_thenGet_thenDelete_flow_succeeds() {
        // 1) 장소 정보로 북마크 추가
        Place place = new Place(
                "kakao-int-1",
                "통합맛집",
                "FD6",
                "음식점",
                "한식",
                "서울특별시 도로명 주소",
                37.11,
                127.22
        );
        BookmarkAddRequest addRequest = new BookmarkAddRequest(place, "회사 근처");

        HttpHeaders addHeaders = new HttpHeaders();
        addHeaders.setContentType(MediaType.APPLICATION_JSON);
        addHeaders.set(USER_ID_HEADER_NAME, DEFAULT_USER_ID);

        HttpEntity<BookmarkAddRequest> addHttpEntity = new HttpEntity<>(addRequest, addHeaders);

        ResponseEntity<Void> addResponse = restTemplate.exchange(
                buildUrl("/api/bookmarks/external"),
                HttpMethod.POST,
                addHttpEntity,
                Void.class
        );
        assertThat(addResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        // 2) 사용자별 북마크 목록 조회
        HttpHeaders listHeaders = new HttpHeaders();
        listHeaders.set(USER_ID_HEADER_NAME, DEFAULT_USER_ID);

        ResponseEntity<BookmarkView[]> listResponse = restTemplate.exchange(
                buildUrl("/api/bookmarks"),
                HttpMethod.GET,
                new HttpEntity<>(listHeaders),
                BookmarkView[].class
        );
        assertThat(listResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

        BookmarkView[] bookmarkViews = listResponse.getBody();
        assertThat(bookmarkViews).isNotNull();
        assertThat(bookmarkViews.length).isGreaterThanOrEqualTo(1);

        long bookmarkId = bookmarkViews[0].bookmarkId();
        long placeId = bookmarkViews[0].placeId();

        // 3) 북마크 단건 조회
        ResponseEntity<BookmarkView> getOneResponse = restTemplate.getForEntity(
                buildUrl("/api/bookmarks/" + bookmarkId),
                BookmarkView.class
        );
        assertThat(getOneResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getOneResponse.getBody()).isNotNull();
        assertThat(getOneResponse.getBody().kakaoPlaceId()).isEqualTo("kakao-int-1");

        // 4) 북마크 삭제
        HttpHeaders deleteHeaders = new HttpHeaders();
        deleteHeaders.set(USER_ID_HEADER_NAME, DEFAULT_USER_ID);

        ResponseEntity<Void> deleteResponse = restTemplate.exchange(
                buildUrl("/api/bookmarks/by-place/" + placeId),
                HttpMethod.DELETE,
                new HttpEntity<>(deleteHeaders),
                Void.class
        );
        assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        // 5) 삭제 확인
        ResponseEntity<BookmarkView[]> listAfterDeleteResponse = restTemplate.exchange(
                buildUrl("/api/bookmarks"),
                HttpMethod.GET,
                new HttpEntity<>(listHeaders),
                BookmarkView[].class
        );
        assertThat(listAfterDeleteResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        BookmarkView[] afterDeleteViews = listAfterDeleteResponse.getBody();
        assertThat(afterDeleteViews).isNotNull();

        boolean containsDeleted =
                java.util.Arrays.stream(afterDeleteViews)
                        .anyMatch(v -> v.bookmarkId() == bookmarkId || v.placeId() == placeId);
        assertThat(containsDeleted).isFalse();
    }
}
