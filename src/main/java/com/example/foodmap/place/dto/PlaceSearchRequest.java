package com.example.foodmap.place.dto;

public class PlaceSearchRequest {
    private final String address;
    private final Double longitude;            // 경도 (x)
    private final Double latitude;             // 위도 (y)
    private final Integer radius;              // 반경 (미터 단위)
    private final Integer size;                // 페이지 크기
    private final Integer page;                // 페이지 번호
    private final String categoryGroupCode;    // 카테고리 코드

    // 생성자
    public PlaceSearchRequest(String address,
                              Double longitude,
                              Double latitude,
                              Integer radius,
                              Integer size,
                              Integer page,
                              String categoryGroupCode) {
        this.address = address;
        this.longitude = longitude;
        this.latitude = latitude;
        this.radius = radius;
        this.size = size;
        this.page = page;
        this.categoryGroupCode = categoryGroupCode;
    }

    // Getter
    public String getAddress() { return address; }
    public Double getLongitude() { return longitude; }
    public Double getLatitude() { return latitude; }
    public Integer getRadius() { return radius; }
    public Integer getSize() { return size; }
    public Integer getPage() { return page; }
    public String getCategoryGroupCode() { return categoryGroupCode; }

    // Builder 패턴 (수동)
    public static class Builder {
        private String address;
        private Double longitude;
        private Double latitude;
        private Integer radius;
        private Integer size;
        private Integer page;
        private String categoryGroupCode;

        public Builder address(String address) { this.address = address; return this; }
        public Builder longitude(Double longitude) { this.longitude = longitude; return this; }
        public Builder latitude(Double latitude) { this.latitude = latitude; return this; }
        public Builder radius(Integer radius) { this.radius = radius; return this; }
        public Builder size(Integer size) { this.size = size; return this; }
        public Builder page(Integer page) { this.page = page; return this; }
        public Builder categoryGroupCode(String categoryGroupCode) { this.categoryGroupCode = categoryGroupCode; return this; }

        public PlaceSearchRequest build() {
            return new PlaceSearchRequest(address, longitude, latitude, radius, size, page, categoryGroupCode);
        }
    }

    public static Builder builder() {
        return new Builder();
    }
}
