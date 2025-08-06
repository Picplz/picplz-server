package com.hm.picplz.domain.auth.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hm.picplz.domain.auth.dto.AuthDto;
import com.hm.picplz.domain.auth.dto.AuthDto.ApplePublicKeys;
import com.hm.picplz.domain.auth.error.AppleErrorCode;
import com.hm.picplz.global.common.service.WebClientService;
import com.hm.picplz.global.error.ExceptionFactory;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
public class AppleTokenService {

    private static final String APPLE_ISSUER = "https://appleid.apple.com";
    private static final String ALG_HEADER_KEY = "alg";
    private static final String KID_HEADER_KEY = "kid";

    @Value("${apple.client-id}")
    private String cid; // iOS 앱의 Bundle ID

    private final WebClientService webClientService;
    private final ObjectMapper objectMapper;
    private final Map<String, PublicKey> keyCache = new ConcurrentHashMap<>();

    /**
     * Apple ID 토큰 검증
     */
    public AuthDto.AppleUserInfo fetchAppleUserInfo(String idToken) {
        // 1. 토큰 헤더 디코드
        String[] tokenParts = idToken.split("\\.");
        String headerJson = new String(Base64.getUrlDecoder().decode(tokenParts[0]));
        try {
            JsonNode header = objectMapper.readTree(headerJson);
            String alg = header.get(ALG_HEADER_KEY).asText();
            String kid = header.get(KID_HEADER_KEY).asText();

            // 2. Apple Public Key 가져오기
            PublicKey publicKey = keyCache.computeIfAbsent(kid, k -> {
                ApplePublicKeys applePublicKeys = webClientService.requestApplePublicKey();
                return getApplePublicKey(alg, kid, applePublicKeys);
            });

            // 3. 토큰 검증
            Claims claims = parseClaims(idToken, publicKey);

            // 5. 결과 반환
            return AuthDto.AppleUserInfo.of(claims.getSubject(), claims.get("email", String.class));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Apple Public Key 가져오기 (캐시 포함)
     */
    private PublicKey getApplePublicKey(String alg, String kid, ApplePublicKeys applePublicKeys) {

        ApplePublicKeys.ApplePublicKey applePublicKey = applePublicKeys.getKeys().stream()
                .filter(key -> key.getAlg().equals(alg))
                .filter(key -> key.getKid().equals(kid))
                .findAny()
                .orElseThrow(() -> ExceptionFactory.of(AppleErrorCode.PUBLIC_KEY_NOT_FOUND));

        PublicKey publicKey = createPublicKey(applePublicKey);
        keyCache.put(kid, publicKey);
        return publicKey;
    }

    /**
     * JWK에서 RSA Public Key 생성
     */
    private PublicKey createPublicKey(ApplePublicKeys.ApplePublicKey applePublicKey) {

        byte[] nBytes = Base64.getUrlDecoder().decode(applePublicKey.getN());
        byte[] eBytes = Base64.getUrlDecoder().decode(applePublicKey.getE());

        BigInteger modulus = new BigInteger(1, nBytes);
        BigInteger exponent = new BigInteger(1, eBytes);

        RSAPublicKeySpec spec = new RSAPublicKeySpec(modulus, exponent);

        try {
            KeyFactory factory = KeyFactory.getInstance("RSA");
            return factory.generatePublic(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw ExceptionFactory.of(AppleErrorCode.FAILED_CREATE_PUBLIC_KEY);
        }
    }

    private Claims parseClaims(String idToken, PublicKey publicKey) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .requireIssuer(APPLE_ISSUER)
                    .requireAudience(cid)
                    .build()
                    .parseClaimsJws(idToken)
                    .getBody();
        } catch (ExpiredJwtException e) {
            throw ExceptionFactory.of(AppleErrorCode.EXPIRED_TOKEN);
        } catch (UnsupportedJwtException | MalformedJwtException | IllegalArgumentException e) {
            throw ExceptionFactory.of(AppleErrorCode.INVALID_TOKEN);
        }
    }
}
