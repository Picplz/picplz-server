package com.hm.picplz.global.common.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.hm.picplz.global.error.ExceptionFactory;
import com.hm.picplz.global.error.GlobalErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.geo.Point;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WebClientService {

    private final WebClient webClient;

    @Value("${geocoder.api.key}")
    private String apiKey;

    /**
     * 좌표를 통해 위치의 법정동 코드 조회
     * @param point 사용자의 x, y 좌표
     * @return 해당 좌표의 법정동 코드
     */
    public Long requestAreaIdByPoint(Point point) {
        JsonNode result = webClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/req/address")
                        .queryParam("service", "address")
                        .queryParam("request", "getAddress")
                        .queryParam("version", "2.0")
                        .queryParam("crs", "epsg:4326")
                        .queryParam("point", point.getX() + "," + point.getY())
                        .queryParam("foramt", "json")
                        .queryParam("type", "parcel")
                        .queryParam("simple", "true")
                        .queryParam("key", apiKey)
                        .build())
                .retrieve()
                .onStatus(HttpStatusCode::is5xxServerError, response ->
                        response.bodyToMono(String.class)
                                .flatMap(body ->
                                        Mono.error(ExceptionFactory.of(GlobalErrorCode.API_INTERNAL_SERVER_ERROR)))
                )
                .bodyToMono(JsonNode.class)
                .block();

        return result
                .path("response")
                .path("result")
                .get(0)
                .path("structure")
                .path("level4LC")
                .asLong();
    }
}
