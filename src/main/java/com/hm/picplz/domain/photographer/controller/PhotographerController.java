package com.hm.picplz.domain.photographer.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.hm.picplz.domain.photographer.annotation.PhotographerOnly;
import com.hm.picplz.domain.photographer.dto.PhotoMoodDto;
import com.hm.picplz.domain.photographer.dto.PhotographerDto;
import com.hm.picplz.domain.photographer.service.PhotographerService;
import com.hm.picplz.domain.product.dto.ProductDto.Detail;
import com.hm.picplz.domain.product.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/photographers")
@Tag(name = "Photographer")
public class PhotographerController {

    private final PhotographerService photographerService;
    private final ProductService productService;

    @Operation(summary = "작가 회원가입")
    @PostMapping
    public PhotographerDto.Detail createPhotographer(@RequestBody PhotographerDto.CreatePhotographerRequest createPhotographerRequest) {
        return photographerService.createPhotographer(createPhotographerRequest);
    }

    @Operation(summary = "작가 로그인")
    @PostMapping("/login")
    public void loginPhotographer(@AuthenticationPrincipal Long memberId) {
        photographerService.cachePhotographerExistence(memberId);
    }

    @PhotographerOnly
    @Operation(summary = "사진 감성 해시 태그 생성")
    @PostMapping("/photo-mood")
    public void addPhotoMood(
        @AuthenticationPrincipal Long memberId,
        @RequestBody PhotoMoodDto.PhotoMoodRequest addPhotoMoodRequestDto) {
        photographerService.addPhotoMood(addPhotoMoodRequestDto, memberId);
    }

    @PhotographerOnly
    @Operation(summary = "사진 감성 해시 태그 삭제")
    @DeleteMapping("/photo-mood")
    public void deletePhotoMood(@AuthenticationPrincipal Long memberId,
        @RequestBody PhotoMoodDto.PhotoMoodRequest deletePhotoMoodRequestDto) {
        photographerService.deletePhotoMood(deletePhotoMoodRequestDto, memberId);
    }

    @Operation(summary = "작가 한명 정보 불러오기")
    @GetMapping("/{photographerId}/info")
    public PhotographerDto.Detail getPhotographerInfo(
        @AuthenticationPrincipal Long memberId,
        @PathVariable Long photographerId) {
        return photographerService.getPhotographerDetail(photographerId, memberId);
    }

    @Operation(summary = "촬영 상품 리스트 조회")
    @GetMapping("/{photographerId}/products")
    public List<Detail> loadProductsByPhotographerId(
            @PathVariable(name = "photographerId") Long photographerId
    ) {
        log.info("촬영 상품 리스트 조회하기");
        return productService.findProductsByPhotographer(photographerId);
    }

    @Operation(summary = "자신의 위치와 활동지역이 같은 작가 조회")
    @GetMapping("/active-area")
    public List<PhotographerDto.Detail> getPhotographersByMemberId(
            @AuthenticationPrincipal Long memberId
    ) {
        return photographerService.getPhotographersByActiveArea(memberId);
    }

    @Operation(summary = "작가의 주 활동지역 변경")
    @PutMapping("/active-area")
    public PhotographerDto.UpdateActiveAreaResponse updateActiveArea(
            @AuthenticationPrincipal Long memberId,
            @RequestBody PhotographerDto.UpdateActiveAreaRequest updateActiveAreaRequestDto) {
        return photographerService.updateActiveArea(memberId, updateActiveAreaRequestDto);
    }
}
