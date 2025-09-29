package com.hm.picplz.domain.member.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.member.dto.MemberDto;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/members")
@Tag(name = "Member")
public class MemberController {

    private final MemberService memberService;

    @Operation(summary = "회원 정보 업데이트")
    @PatchMapping(value = "/info")
    public MemberDto.MemberInfoResponse updateMemberInfo(@RequestBody MemberDto.UpdateMemberInfoRequest updateMemberInfoRequest) {
        return memberService.updateMemberInfo(updateMemberInfoRequest);
    }

    @Operation(summary = "카카오 로그인 거치지 않는 테스트용 회원 생성 api", tags = "test")
    @PostMapping(value = "/test")
    public MemberDto.MemberInfoResponse createMemberTest(@RequestBody MemberDto.CreateMemberTest createMemberRequest) {
        return memberService.createMemberTest(createMemberRequest);
    }

    @Operation(summary = "위치 업데이트")
    @PostMapping(value = "/location")
    public void updateMemberLocation(
            @AuthenticationPrincipal Long memberId,
            @RequestBody MemberDto.UpdateMemberLocationRequest updateMemberLocationRequest
    ) {
        log.info("사용자 위치 업데이트");
        memberService.updateLocation(memberId, updateMemberLocationRequest);
    }

    @Operation(summary = "회원 닉네임 중복검사")
    @GetMapping("/nickname")
    public void checkNickname(@RequestParam String nickname) {
		memberService.checkNickname(nickname);
	}

    @Operation(summary = "유저 정보 하나 얻기")
    @GetMapping("/{memberId}/info")
    public MemberDto.MemberInfoResponse getMemberInfo(@PathVariable Long memberId) {
        return memberService.getMemberInfo(memberId);
    }

}
