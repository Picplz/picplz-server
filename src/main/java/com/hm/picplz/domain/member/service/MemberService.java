package com.hm.picplz.domain.member.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import com.hm.picplz.domain.member.domain.SocialProvider;
import com.hm.picplz.domain.photographer.helper.PhotographerHelper;
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
import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.exception.MemberErrorCode;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {
    public static final String GEO_KEY = "members:locations";
    private static final Pattern NICKNAME_PATTERN = Pattern.compile("^(?!\\s)(?!.*\\s$)[a-zA-Z0-9가-힣]{2,15}$");

    private final MemberRepository memberRepository;
    private final RedisTemplate<String, Object> redisTemplate;

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
     * 회원 정보 수정 api
     * @param updateMemberInfoRequest 수정 정보
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

    public void updateLocation(Long memberId, MemberDto.UpdateMemberLocationRequest request) {
        redisTemplate.opsForGeo().add(GEO_KEY, new RedisGeoCommands.GeoLocation<>(
                memberId, new Point(request.getLongitude(), request.getLatitude())));

        // TODO: role이 photographer인지 customer인지 파악 후 부가 정보로 추가 + 활동중 여부
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

    private boolean checkNicknameForUpdate(String nickname, Member member) {
        return memberRepository.existsByNicknameIsAndIdNot(nickname, member.getId());
    }
}
