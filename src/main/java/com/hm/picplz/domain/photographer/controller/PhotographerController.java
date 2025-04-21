package com.hm.picplz.domain.photographer.controller;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    @Operation(summary = "회원가입 시 작가 생성 api")
    @PostMapping
    public void createPhotographer(
        @AuthenticationPrincipal Long memberId,
        @RequestBody PhotographerDto.Create createPhotographerRequestDto) {
        photographerService.createPhotographer(memberId, createPhotographerRequestDto);
    }

    @Operation(summary = "작가 로그인")
    @PostMapping("/login")
    public void loginPhotographer(@AuthenticationPrincipal Long memberId) {
        photographerService.cachePhotographerExistence(memberId);
    }


    // @PhotographerOnly
    // @Operation(summary = "작가 경력 추가")
    // @PostMapping("/career")
    // public void updatePhotographerCareer(
    //     @AuthenticationPrincipal Long memberId,
    //     @RequestBody PhotographerDto.AddCareer addCareerRequestDto) {
    //     photographerService.addCareer(addCareerRequestDto, memberId);
    // }
    //
    // @PhotographerOnly
    // @Operation(summary = "작가 경력 기간 수정")
    // @PatchMapping("/period")
    // public void updatePhotographerCareerPeriod(
    //     @AuthenticationPrincipal Long memberId,
    //     @RequestBody PhotographerDto.UpdateCareerPeriod updateCareerPeriodRequestDto) {
    //     photographerService.updateCareerPeriod(updateCareerPeriodRequestDto, memberId);
    // }

    @PhotographerOnly
    @Operation(summary = "사진 감성 해시 태그 생성")
    @PostMapping("/photo-mood")
    public PhotoMoodDto.PhotoMoodRes addPhotoMood(
        @AuthenticationPrincipal Long memberId,
        @RequestBody PhotoMoodDto.AddPhotoMood addPhotoMoodRequestDto) {
        return photographerService.addPhotoMoods(addPhotoMoodRequestDto, memberId);
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
}
