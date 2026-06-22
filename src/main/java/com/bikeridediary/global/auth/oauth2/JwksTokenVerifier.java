package com.bikeridediary.global.auth.oauth2;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTClaimsVerifier;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class JwksTokenVerifier {

    private static final Map<String, CachedJwkSet> jwksCache = new ConcurrentHashMap<>();
    private static final long CACHE_TTL_MS = 24 * 60 * 60 * 1000; // 24시간

    public static JWTClaimsSet verify(String idToken, String jwksUrl, String expectedIssuer, String expectedAudience) {
        try {
            JWKSet jwkSet = getCachedJwkSet(jwksUrl);

            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();

            processor.setJWSKeySelector(
                    new JWSVerificationKeySelector<>(JWSAlgorithm.RS256, new ImmutableJWKSet<>(jwkSet))
            );

            processor.setJWTClaimsSetVerifier(new DefaultJWTClaimsVerifier<>(
                    new JWTClaimsSet.Builder()
                            .issuer(expectedIssuer)
                            .audience(expectedAudience)
                            .build(),
                    new HashSet<>(Arrays.asList("sub", "iat", "exp"))
            ));

            return processor.process(idToken, null);
        } catch (Exception e) {
            log.error("JWT 검증 실패 - issuer: {}", expectedIssuer, e);
            throw new RuntimeException("ID 토큰 검증 실패", e);
        }
    }

    private static JWKSet getCachedJwkSet(String jwksUrl) {
        CachedJwkSet cached = jwksCache.get(jwksUrl);
        if (cached != null && !cached.isExpired()) {
            return cached.jwkSet;
        }

        try {
            JWKSet jwkSet = JWKSet.load(new URL(jwksUrl));
            jwksCache.put(jwksUrl, new CachedJwkSet(jwkSet, System.currentTimeMillis()));
            return jwkSet;
        } catch (Exception e) {
            if (cached != null) {
                log.warn("JWKS 갱신 실패, 캐시된 키 사용 - url: {}", jwksUrl, e);
                return cached.jwkSet;
            }
            throw new RuntimeException("JWKS 로드 실패: " + jwksUrl, e);
        }
    }

    private record CachedJwkSet(JWKSet jwkSet, long fetchedAt) {
        boolean isExpired() {
            return System.currentTimeMillis() - fetchedAt > CACHE_TTL_MS;
        }
    }
}
