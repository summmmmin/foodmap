package com.example.foodmap.external.kakao;

import com.example.foodmap.external.kakao.dto.KakaoAddressDtos.AddressSearchResponse;
import com.example.foodmap.external.kakao.dto.KakaoCategoryDtos.CategorySearchResponse;
import com.example.foodmap.place.dto.Place;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Optional;

@Component
public class KakaoLocalClient {

    private final RestClient client;

    public KakaoLocalClient(
            RestClient.Builder builder,
            @Value("${kakao.api.base-url}") String baseUrl,
            @Value("${kakao.api.key:dummy}") String apiKey,
            @Value("${kakao.api.timeout-ms:3000}") int timeoutMs
    ) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(timeoutMs);
        factory.setReadTimeout(timeoutMs);

        this.client = builder
                .baseUrl(baseUrl)
                .requestFactory(factory)
                .defaultHeader("Authorization", "KakaoAK " + apiKey)
                .build();
    }

    // 주소 -> 좌표 변환
    public Optional<double[]> geocodeAddress(String query) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("query", query);

        AddressSearchResponse body = client.get()
                .uri(uri -> uri.path("/v2/local/search/address.json").queryParams(params).build())
                .retrieve()
                .body(AddressSearchResponse.class);

        if (body == null || body.documents() == null || body.documents().isEmpty()) {
            return Optional.empty();
        }
        var d = body.documents().getFirst();
        return Optional.of(new double[]{ parseDouble(d.y()), parseDouble(d.x()) }); // [lat, lon]
    }

    // 좌표로 카테고리(음식점) 검색
    public List<Place> searchCategory(String categoryGroupCode,
                                             double lon, double lat,
                                             int radius, int size, int page) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("category_group_code", categoryGroupCode);
        params.add("x", Double.toString(lon));
        params.add("y", Double.toString(lat));
        params.add("radius", Integer.toString(radius)); // 0~20000(m)
        params.add("size", Integer.toString(size));     // 1~45
        params.add("page", Integer.toString(page));     // 1~45

        CategorySearchResponse body = client.get()
                .uri(uri -> uri.path("/v2/local/search/category.json").queryParams(params).build())
                .retrieve()
                .body(CategorySearchResponse.class);

        if (body == null || body.documents() == null) return List.of();

        return body.documents().stream().map(d ->
                new Place(
                        d.id(),
                        d.place_name(),
                        d.category_group_code(),
                        d.category_group_name(),
                        d.category_name(),
                        d.road_address_name(),
                        parseDouble(d.y()),
                        parseDouble(d.x())
                )
        ).toList();
    }

    private static double parseDouble(String s) {
        try { return Double.parseDouble(s); } catch (Exception e) { return 0.0; }
    }
}
