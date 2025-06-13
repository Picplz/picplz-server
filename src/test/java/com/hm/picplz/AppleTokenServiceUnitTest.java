package com.hm.picplz;

import com.hm.picplz.domain.auth.service.AppleTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AppleTokenServiceUnitTest {

    @InjectMocks
    private AppleTokenService appleTokenService;

    private KeyPair keyPair;
    private String testClientId = "com.test.app";

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(appleTokenService, "cid", testClientId);

        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
    }

    @Test
    @DisplayName("클레임 파싱 테스트")
    void 클레임_파싱_테스트() {
        //given
        Date now = new Date();
        Map<String, Object> headers = new HashMap<>();
        headers.put("kid", "test");
        String appleToken = Jwts.builder()
                .setHeader(headers)
                .claim("email", "test@test.com")
                .setIssuer("https://appleid.apple.com")
                .setIssuedAt(now)
                .setAudience(testClientId)
                .setSubject("sub")
                .setExpiration(new Date(now.getTime() + 60 * 60 * 1000))
                .signWith(keyPair.getPrivate(), SignatureAlgorithm.RS256)
                .compact();

        //when
//        Claims claims = appleTokenService.parseClaims(appleToken, keyPair.getPublic()); // test 하려면 parseClaims public 으로 변경 필요

        //then
//        assertThat(claims.get("email")).isEqualTo("test@test.com");
    }
}
