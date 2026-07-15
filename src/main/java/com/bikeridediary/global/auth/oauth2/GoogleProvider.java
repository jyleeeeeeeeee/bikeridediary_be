package com.bikeridediary.global.auth.oauth2;

import com.nimbusds.jwt.JWTClaimsSet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GoogleProvider implements OAuth2Provider {

    private static final String JWKS_URL = "https://www.googleapis.com/oauth2/v3/certs";
    private static final String ISSUER = "https://accounts.google.com";

    private final GoogleOAuth2Properties properties;

    @Override
    public OAuth2UserInfo getUserInfo(String identityToken) {
        JWTClaimsSet claims = JwksTokenVerifier.verify(identityToken, JWKS_URL, ISSUER, properties.clientId());

        String sub = claims.getSubject();
        String email = (String) claims.getClaim("email");
        String name = (String) claims.getClaim("name");
        String picture = (String) claims.getClaim("picture");

        return OAuth2UserInfo.fromGoogle(sub, email, name, picture);
    }

    @Override
    public String getProviderName() {
        return "google";
    }
}
