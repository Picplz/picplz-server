package com.hm.picplz.domain.portfolio.controller;

import com.hm.picplz.domain.portfolio.dto.PortfolioDto;
import com.hm.picplz.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "portfolios")
@Tag(name = "Portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 생성")
    @PostMapping
    public PortfolioDto.PortfolioResponse createPortfolio(
            @AuthenticationPrincipal Long memberId,
            @RequestBody PortfolioDto.CreatePortfolioRequest request) {
        return portfolioService.createPortfolio(memberId, request);
    }

    @Operation(summary = "포트폴리오 단일 조회")
    @GetMapping("/{portfolioId}")
    public PortfolioDto.PortfolioResponse getPortfolio(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long portfolioId) {
        return portfolioService.getPortfolio(memberId, portfolioId);
    }

    @Operation(summary = "스크랩 추가")
    @PostMapping("/{portfolioId}/scrap")
    public ResponseEntity<?> createScrap(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long portfolioId) {
        portfolioService.createScrap(memberId, portfolioId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "스크랩 삭제")
    @DeleteMapping("/{portfolioId}/scrap")
    public ResponseEntity<?> deleteScrap(
            @AuthenticationPrincipal Long memberId,
            @PathVariable Long portfolioId) {
        portfolioService.deleteScrap(memberId, portfolioId);
        return ResponseEntity.noContent().build();
    }
}
