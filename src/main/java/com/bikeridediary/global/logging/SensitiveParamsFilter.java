package com.bikeridediary.global.logging;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 요청 파라미터 Map에서 민감 필드 제거.
 * 헤더에 담긴 API Key/Secret은 파라미터에 넣지 않으므로 방어적 성격이지만,
 * 향후 누군가 실수로 파라미터에 시크릿을 담는 경우를 대비.
 */
public final class SensitiveParamsFilter {

    private SensitiveParamsFilter() {}

    /** 제거 대상 키 (소문자 비교) */
    private static final Set<String> BLOCKED_KEYS = Set.of(
            "apikey", "api_key", "api-key",
            "clientid", "client_id", "client-id",
            "clientsecret", "client_secret", "client-secret",
            "appid",   // OpenWeather
            "x-ncp-apigw-api-key", "x-ncp-apigw-api-key-id"
    );

    public static Map<String, Object> filter(Map<String, Object> params) {
        if (params == null || params.isEmpty()) return Map.of();
        Map<String, Object> result = new HashMap<>(params.size());
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String keyLower = entry.getKey() == null ? "" : entry.getKey().toLowerCase();
            if (!BLOCKED_KEYS.contains(keyLower)) {
                result.put(entry.getKey(), entry.getValue());
            }
        }
        return result;
    }
}