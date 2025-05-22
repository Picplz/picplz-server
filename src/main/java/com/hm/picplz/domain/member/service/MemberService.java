package com.hm.picplz.domain.member.service;

import java.util.List;
import java.util.regex.Pattern;

import com.hm.picplz.domain.member.domain.SocialProvider;
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
import com.hm.picplz.domain.member.dto.MemberDto;
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

    /**
     * 고객, 작가 회원가입 시 멤버 데이터 추가
     * @param createMemberRequest
     * @return 멤버 entity
     */
    @Transactional
    public Member createMember(MemberDto.CreateMemberRequest createMemberRequest) {
        Member member = Member.builder()
            .nickname(createMemberRequest.getNickname())
            .socialEmail(createMemberRequest.getSocialEmail())
            .role(createMemberRequest.getRole())
            .socialProvider(createMemberRequest.getSocialProvider())
            .attributeCode(createMemberRequest.getAttributeCode())
            .profileImage(createMemberRequest.getProfileImage())
            .build();

        return memberRepository.save(member);
    }

    /**
     * 닉네임과 프로필 이미지 수정 api
     * @param updateMemberInfoRequest
     * @return 수정된 멤버 정보
     */
    @Transactional
    public MemberDto.MemberInfoResponse updateMemberInfo(MemberDto.UpdateMemberInfoRequest updateMemberInfoRequest) {
        Member member = memberRepository.findById(updateMemberInfoRequest.getId())
                .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

        // 닉네임 중복 확인
        if (checkNicknameForUpdate(updateMemberInfoRequest.getNickname(), member)) {
            member.updateNickname(updateMemberInfoRequest.getNickname());
        }
        member.updateProfileImage(updateMemberInfoRequest.getProfileImage());
        member.updateInstagram(updateMemberInfoRequest.getInstagram());
        member.updateIntroduction(updateMemberInfoRequest.getIntroduction());

        return MemberDto.MemberInfoResponse.of(member);
    }

    private boolean checkNicknameForUpdate(String nickname, Member member) {
        return memberRepository.existsByNicknameIsAndIdNot(nickname, member.getId());
    }

    public void updateLocation(MemberDto.UpdateMemberLocationRequest request) {
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
    public MemberDto.MemberInfoResponse createMemberTest(MemberDto.CreateMemberTest createMemberRequest) {
        Member member = Member.builder()
                .birth(createMemberRequest.getBirth())
                .nickname(createMemberRequest.getNickname())
                .role(createMemberRequest.getRole())
                .socialEmail(createMemberRequest.getSocialEmail())
                .profileImage(createMemberRequest.getProfileImage())
                .attributeCode(null)
                .socialProvider(null)
                .build();

        memberRepository.save(member);

        return MemberDto.MemberInfoResponse.of(member);
    }

    /**
     * 닉네임 패턴 및 중복 여부 확인
     * @param nickname 확인하려는 닉네임
     */
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

    @Transactional
    public MemberDto.UpdateNicknameResponse updateNickname(MemberDto.UpdateNicknameRequest updateNicknameRequest) {
        Member member = memberRepository.findById(updateNicknameRequest.getMemberId())
                .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));
        checkNickname(updateNicknameRequest.getNickname());
        member.updateNickname(updateNicknameRequest.getNickname());

        return MemberDto.UpdateNicknameResponse.of(member);
    }

    /**
     * 정제된 멤버 정보 반환 메서드
     * @param id 조회하려는 멤버의 pk
     * @return 정제된 멤버 정보
     */
    public MemberDto.MemberInfoResponse getMemberInfo(Long id) {
        return MemberDto.MemberInfoResponse.of(getMemberById(id));
    }

    /**
     * 실제 멤버 entity 데이터
     * @param id 조회하려는 멤버의 pk
     * @return DB에 저장된 멤버의 모든 정보
     */
    private Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
