package com.hm.picplz.domain.photographer.helper;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.global.error.ExceptionFactory;

import java.util.List;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class PhotographerHelper {

    private final PhotographerRepository photographerRepository;

    @Transactional
    public List<PhotographerDto.Card> getPhotographerCardByMemberGeoInfo(List<GeoResult<GeoLocation<Object>>> results) {
        return results.stream()
                .map(result -> {
                    Long memberId = Long.parseLong(result.getContent().getName().toString());
                    Photographer photographer = photographerRepository.findByMemberId(memberId)
                            .orElseThrow(() ->  ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
                    return photographer != null ? PhotographerDto.Card.of(photographer, (long) result.getDistance().getValue()) : null;
                })
                .filter(Objects::nonNull)
                .toList();
    }
}