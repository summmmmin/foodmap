package com.example.foodmap.common;

public final class CacheKeys {
    private CacheKeys() {}

    public static String normalizeAddress(String raw) {
        if (raw == null) return "";
        String s = raw.trim();
        s = s.replaceAll("\\s+", " ");
        s = s.replaceAll("[,]+", "");
        s = s.toLowerCase();
        return s;
    }

    // 주소→좌표 캐시 키
    public static String geoAddrKey(String address) {
        return "geo:addr:" + normalizeAddress(address);
    }

    // 좌표 반경 검색 결과 캐시 키
    public static String nearKey(double x, double y, int radius, int size, int page, String cg) {
        return String.format("place:near:x=%.6f:y=%.6f:r=%d:s=%d:p=%d:cg=%s", x, y, radius, size, page, cg);
    }
}
