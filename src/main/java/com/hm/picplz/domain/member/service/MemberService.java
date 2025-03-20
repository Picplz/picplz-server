package com.hm.picplz.domain.member.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.member.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.dto.request.CreateMemberRequest;
import com.hm.picplz.domain.member.dto.request.UpdateMemberInfoRequest;
import com.hm.picplz.domain.member.dto.request.UpdateMemberLocationRequest;
import com.hm.picplz.domain.member.dto.response.MemberInfoResponse;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.helper.PhotographerHelper;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^(?!\\s)(?!.*\\s$)[a-zA-Z0-9가-힣]{2,15}$");
    private static final String GEO_KEY = "members:locations";

    private final MemberRepository memberRepository;
    private final PhotographerHelper photographerHelper;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public MemberInfoResponse updateMemberInfo(UpdateMemberInfoRequest updateMemberInfoRequest) {
        Member member = memberRepository.findById(updateMemberInfoRequest.getId())
                .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

        member.updateNickname(updateMemberInfoRequest.getNickname());
        member.updateProfileImage(updateMemberInfoRequest.getProfileImage());

        return new MemberInfoResponse(member);
    }

    public void updateLocation(UpdateMemberLocationRequest request) {
        redisTemplate.opsForGeo().add(GEO_KEY, new RedisGeoCommands.GeoLocation<>(
                request.getMemberId(), new Point(request.getLongitude(), request.getLatitude())));

        // TODO: role이 photographer인지 customer인지 파악 후 부가 정보로 추가 + 활동중 여부
    }

    /**
     *
     * @param longitude 기준 경도
     * @param latitude 기준 위도
     * @param distance 반경 (단위: km)
     * @return 검색된 데이터 목록
     */
    public List<PhotographerDto.Card> findPhotographersWithinRadius(double longitude, double latitude, long distance) {
        Circle circle = new Circle(new Point(longitude, latitude), distance * 1000);    // 반경 (단위: m)

        // 검색 옵션 (거리 포함, 가까운 순)
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        List<GeoResult<GeoLocation<Object>>> results = redisTemplate
                .opsForGeo()
                .radius(GEO_KEY, circle, args)
                .getContent();

        return photographerHelper.getPhotographerCardByMemberGeoInfo(results);
    }

    @Transactional
    public MemberInfoResponse createMemberTest(CreateMemberRequest createMemberRequest) {
        Member member = Member.builder()
                .birth(createMemberRequest.getBirth())
                .name(createMemberRequest.getName())
                .nickname(createMemberRequest.getNickname())
                .role(createMemberRequest.getRole())
                .kakaoEmail(createMemberRequest.getKakaoEmail())
                .profileImage(createMemberRequest.getProfileImage())
                .attributeCode(null)
                .provider(null)
                .build();

        memberRepository.save(member);

        return new MemberInfoResponse(member);
    }


    public void checkNickname(String nickname) {
        /*
        * - 닉네임의 처음과 마지막 부분 공백 사용 불가
        * - 한글, 영문, 숫자 입력 가능 (2-15자)
        * - 이모티콘, 특수문자 사용 불가
        * - 중복 닉네임 불가
        * */
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) throw ExceptionFactory.of(MemberErrorCode.NOT_VALID_NICKNAME);
        if (memberRepository.existsByNicknameIs(nickname)) {
            throw ExceptionFactory.of(MemberErrorCode.DUPLICATE_NICKNAME);
        }
    }

    public MemberInfoResponse getMemberInfo(Long id) {
        return new MemberInfoResponse(getMemberById(id));
    }

    private Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
