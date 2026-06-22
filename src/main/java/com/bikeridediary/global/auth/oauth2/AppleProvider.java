package com.bikeridediary.global.auth.oauth2;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class AppleProvider implements OAuth2Provider {

    private static final String JWKS_URL = "https://appleid.apple.com/auth/keys";
    private static final String ISSUER = "https://appleid.apple.com";

    @Value("${apple.client-id}")
    private String clientId;

    @Override
    public OAuth2UserInfo getUserInfo(String identityToken) {
        JWTClaimsSet claims = JwksTokenVerifier.verify(identityToken, JWKS_URL, ISSUER, clientId);

        String sub = claims.getSubject();
        String email = (String) claims.getClaim("email");

        return OAuth2UserInfo.fromApple(sub, email);
    }

    @Override
    public String getProviderName() {
        return "apple";
    }
}
