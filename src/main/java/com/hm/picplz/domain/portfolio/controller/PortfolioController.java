package com.hm.picplz.domain.portfolio.controller;

import com.hm.picplz.domain.portfolio.dto.PortfolioDto;
import com.hm.picplz.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping(value = "portfolios")
@Tag(name = "Portfolio")
public class PortfolioController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 생성")
    @PostMapping
    public PortfolioDto.CreatePortfolioResponse createPortfolio(
            @AuthenticationPrincipal Long memberId,
            @RequestBody PortfolioDto.CreatePortfolioRequest request) {
        return portfolioService.create(memberId, request);
    }

}
