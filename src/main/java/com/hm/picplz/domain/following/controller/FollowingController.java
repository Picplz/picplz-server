package com.hm.picplz.domain.following.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.hm.picplz.domain.following.dto.FollowingDto;
import com.hm.picplz.domain.following.service.FollowingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/following")
@Tag(name = "Following")
public class FollowingController {

    private final FollowingService followingService;

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "작가 팔로우")
    @PostMapping("/{photographerId}")
    public void follow(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long photographerId) {
        followingService.follow(memberId, photographerId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "작가 언팔로우")
    @DeleteMapping("/{photographerId}")
    public void unfollow(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long photographerId) {
        followingService.unfollow(memberId, photographerId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "작가 팔로우 여부 확인")
    @GetMapping("/{photographerId}")
    public FollowingDto.IsFollowingResponse checkFollowing(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long photographerId) {
        return followingService.checkFollowing(memberId, photographerId);
    }

    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "팔로우한 작가 목록 조회")
    @GetMapping("/photographers")
    public List<FollowingDto.PhotographerCard> getFollowingPhotographers(
            @AuthenticationPrincipal Long memberId,
            @Parameter(description = "정렬 기준: LATEST(최신순)")
            @RequestParam(defaultValue = "LATEST") String sort) {
        return followingService.getFollowingPhotographers(memberId, sort);
    }
}
