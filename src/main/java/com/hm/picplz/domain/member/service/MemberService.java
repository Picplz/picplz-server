package com.hm.picplz.domain.member.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.hm.picplz.domain.auth.jwt.JwtTokenProvider;
import com.hm.picplz.domain.auth.jwt.JwtTokenResponseDto;
import com.hm.picplz.domain.auth.service.AuthService;
import com.hm.picplz.domain.auth.service.TokenBlacklistService;
import com.hm.picplz.domain.member.domain.SocialProvider;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.connection.RedisGeoCommands.GeoLocation;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.member.repository.MemberRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.global.common.entity.YesNo;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^(?!\\s)(?!.*\\s$)[a-zA-Z0-9가-힣]{2,15}$");
    private static final String GEO_KEY = "members:locations";

    private final MemberRepository memberRepository;
    private final PhotographerRepository photographerRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthService authService;
    private final TokenBlacklistService tokenBlacklistService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 고객, 작가 회원가입 시 멤버 데이터 추가
     * @param createMemberRequest 회원가입 필수 데이터
     * @return 멤버 entity
     */
    @Transactional
    public Member createMember(MemberDto.CreateMemberRequest createMemberRequest) {
        Member member = Member.builder()
            .nickname(createMemberRequest.getNickname())
            .socialEmail(createMemberRequest.getSocialEmail())
            .role(createMemberRequest.getRole())
            .socialProvider(createMemberRequest.getSocialProvider())
            .socialCode(createMemberRequest.getSocialCode())
            .profileImage(createMemberRequest.getProfileImage())
            .build();

        return memberRepository.save(member);
    }

    /**
     * social_code로 기존 Member 조회 또는 새로 생성
     * 작가-고객 전환 시 1개의 Member에 2개의 프로필을 연결하기 위한 메서드
     */
    @Transactional
    public Member findOrCreateMember(MemberDto.CreateMemberRequest createMemberRequest) {
        // social_code + social_provider로 기존 Member 조회
        Optional<Member> existingMember = memberRepository.findBySocialCodeAndSocialProvider(
            createMemberRequest.getSocialCode(),
            createMemberRequest.getSocialProvider()
        );

        // 기존 Member가 있으면 재사용, 없으면 새로 생성
        return existingMember.orElseGet(() -> createMember(createMemberRequest));
    }

    /**
     * 회원 정보 수정 api
     * @param updateMemberInfoRequest
     * @return 수정된 멤버 정보
     */
    @Transactional
    public MemberDto.MemberInfoResponse updateMemberInfo(MemberDto.UpdateMemberInfoRequest updateMemberInfoRequest) {
        Member member = memberRepository.findById(updateMemberInfoRequest.getId())
                .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

        // 프로필 사진 수정
        Optional.ofNullable(updateMemberInfoRequest.getProfileImage())
                .ifPresent(member::updateProfileImage);
        // 닉네임 수정
        Optional.ofNullable(updateMemberInfoRequest.getNickname()).ifPresent(nickname -> {
            if (!checkNicknameForUpdate(nickname, member)) { // 해당 아이디가 아니면서, 해당 닉네임을 가지고 있는 사람이 없다면 변경
                member.updateNickname(nickname);
            }
        });
        // 인스타그램 수정
        Optional.ofNullable(updateMemberInfoRequest.getInstagram())
                .ifPresent(member::updateInstagram);
        // 소개말 수정
        Optional.ofNullable(updateMemberInfoRequest.getIntroduction())
                .ifPresent(member::updateIntroduction);

        return MemberDto.MemberInfoResponse.from(member);
    }

    private boolean checkNicknameForUpdate(String nickname, Member member) {
        return memberRepository.existsByNicknameIsAndIdNot(nickname, member.getId());
    }

    public void updateLocation(MemberDto.UpdateMemberLocationRequest request) {
        redisTemplate.opsForGeo().add(GEO_KEY, new RedisGeoCommands.GeoLocation<>(
                String.valueOf(request.getMemberId()), new Point(request.getLongitude(), request.getLatitude())));

        // TODO: role이 photographer인지 customer인지 파악 후 부가 정보로 추가 + 활동중 여부
    }

    /**
     *
     * @param longitude 기준 경도
     * @param latitude 기준 위도
     * @param distance 반경 (단위: km)
     * @return 검색된 데이터 목록
     */
    @Transactional
    public List<PhotographerDto.Card> findPhotographersWithinRadius(double longitude, double latitude, long distance) {
        Circle circle = new Circle(new Point(longitude, latitude), distance * 1000d);    // 반경 (단위: m)

        // 검색 옵션 (거리 포함, 가까운 순)
        RedisGeoCommands.GeoRadiusCommandArgs args = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()
                .sortAscending();

        GeoResults<GeoLocation<Object>> geoResults = redisTemplate
            .opsForGeo()
            .radius(GEO_KEY, circle, args);

        List<GeoResult<GeoLocation<Object>>> results =
            geoResults != null ? geoResults.getContent() : Collections.emptyList();

        // 현재 위치 반경 기준 활동중인 작가들
        List<PhotographerDto.Card> list = results.stream()
                .filter(result -> result.getContent().getName() != null)
                .map(result -> {
                    Long memberId = Long.parseLong(result.getContent().getName().toString());
                    return photographerRepository.findByMemberId(memberId)
                            .filter(p -> p.getActive() == YesNo.Y)
                            .map(p -> PhotographerDto.Card.of(p, (long) result.getDistance().getValue()))
                            .orElse(null);
                })
                .filter(card -> card != null)
                .limit(5)
                .toList();

        if(list.isEmpty()) {
            //TODO: 관심 고객 많은 작가 return
        }

        return list;
    }

    @Transactional
    public MemberDto.MemberInfoResponse createMemberTest(MemberDto.CreateMemberTest createMemberRequest) {
        Member member = Member.builder()
                .birth(createMemberRequest.getBirth())
                .nickname(createMemberRequest.getNickname())
                .role(createMemberRequest.getRole())
                .socialEmail(createMemberRequest.getSocialEmail())
                .profileImage(createMemberRequest.getProfileImage())
                .socialCode("aaa")
                .socialProvider(SocialProvider.KAKAO)
                .build();

        memberRepository.save(member);

        return MemberDto.MemberInfoResponse.from(member);
    }

    /**
     * 닉네임 패턴 검사
     * - 앞뒤 공백 불가, 한글/영문/숫자 2~15자, 이모티콘·특수문자 불가
     */
    public void validateNicknameFormat(String nickname) {
        if (!NICKNAME_PATTERN.matcher(nickname).matches()) {
            throw ExceptionFactory.of(MemberErrorCode.NOT_VALID_NICKNAME);
        }
    }

    /**
     * 닉네임 중복 검사
     */
    public void checkNicknameDuplicate(String nickname) {
        if (memberRepository.existsByNicknameIs(nickname)) {
            throw ExceptionFactory.of(MemberErrorCode.DUPLICATE_NICKNAME);
        }
    }

    /**
     * 닉네임 패턴 및 중복 여부 확인 (내부 공통 사용)
     */
    public void checkNickname(String nickname) {
        validateNicknameFormat(nickname);
        checkNicknameDuplicate(nickname);
    }

    /**
     * 정제된 멤버 정보 반환 메서드
     * @param id 조회하려는 멤버의 pk
     * @return 정제된 멤버 정보
     */
    public MemberDto.MemberInfoResponse getMemberInfo(Long id) {
        return MemberDto.MemberInfoResponse.from(getMemberById(id));
    }

    /**
     * 멤버의 좌표 반환 메서드
     * @param id 조회하려는 멤버의 pk
     * @return 멤버의 좌표 정보
     */
    public Point getMemberLocation(Long id) {
        return Optional.ofNullable(redisTemplate.opsForGeo().position(GEO_KEY, id))
                .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_LOCATION_NOT_FOUND))
                .get(0);
    }

    /**
     * 실제 멤버 entity 데이터
     * @param id 조회하려는 멤버의 pk
     * @return DB에 저장된 멤버의 모든 정보
     */
    public Member getMemberById(Long id) {
        return memberRepository.findById(id).orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    /**
     * 회원의 역할 전환 (작가 ↔ 고객)
     * 전환하려는 역할의 프로필이 이미 존재해야 전환 가능
     * @param memberId  역할을 전환할 회원 ID
     * @param targetRole 전환하려는 역할
     * @param authHeader 현재 요청의 Authorization 헤더 (기존 토큰 무효화에 사용)
     * @return 새로운 JWT 토큰과 프로필 정보
     */
    @Transactional
    public MemberDto.SwitchRoleResponse switchRole(Long memberId, Role targetRole, String authHeader) {
        // 1. Member 조회
        Member member = memberRepository.findById(memberId)
            .orElseThrow(() -> ExceptionFactory.of(MemberErrorCode.MEMBER_NOT_FOUND));

        // 2. 전환하려는 역할의 프로필이 있는지 확인
        if (targetRole == Role.PHOTOGRAPHER) {
            if (member.getPhotographer() == null) {
                throw ExceptionFactory.of(MemberErrorCode.PHOTOGRAPHER_PROFILE_NOT_FOUND);
            }
        } else if (targetRole == Role.CUSTOMER) {
            if (member.getCustomer() == null) {
                throw ExceptionFactory.of(MemberErrorCode.CUSTOMER_PROFILE_NOT_FOUND);
            }
        }

        // 3. 역할 전환 시 기존 토큰 즉시 무효화
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String oldToken = authHeader.substring(7);
            long remaining = jwtTokenProvider.getRemainingExpiration(oldToken);
            if (remaining > 0) {
                tokenBlacklistService.blacklistToken(oldToken, remaining);
            }
        }

        // 4. Member의 role 업데이트
        member.updateRole(targetRole);

        // 5. 새로운 JWT 토큰 발급
        JwtTokenResponseDto tokenDto = authService.generateTokens(member);

        // 6. 응답 생성
        return MemberDto.SwitchRoleResponse.builder()
            .accessToken(tokenDto.getAccessToken())
            .refreshToken(tokenDto.getRefreshToken())
            .currentRole(member.getRole())
            .hasPhotographerProfile(member.hasPhotographerProfile())
            .hasCustomerProfile(member.hasCustomerProfile())
            .build();
    }
}
