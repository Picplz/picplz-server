package com.hm.picplz.domain.portfolio.service;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.service.PhotographerService;
import com.hm.picplz.domain.portfolio.domain.Portfolio;
import com.hm.picplz.domain.portfolio.domain.PortfolioPhoto;
import com.hm.picplz.domain.portfolio.dto.PortfolioDto;
import com.hm.picplz.domain.portfolio.exception.PortfolioErrorCode;
import com.hm.picplz.domain.portfolio.repository.PortfolioRepository;
import com.hm.picplz.domain.portfolio.repository.ScrapRepository;
import com.hm.picplz.global.error.ExceptionFactory;
import com.hm.picplz.infra.s3.S3PresignedUrlService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PhotographerService photographerService;
    private final PortfolioRepository portfolioRepository;
    private final ScrapRepository scrapRepository;
    private final S3PresignedUrlService s3PresignedUrlService;

    /**
     * 포트폴리오 생성
     * @param memberId 멤버 아이디
     * @param request 포트폴리오 생성 요청
     * @return 포트폴리오 생성 결과
     */
    @Transactional
    public PortfolioDto.PortfolioResponse createPortfolio(Long memberId, PortfolioDto.CreatePortfolioRequest request) {
        Photographer photographer = photographerService.getPhotographerByMemberId(memberId);

        Portfolio portfolio = Portfolio.builder()
                .photographer(photographer)
                .location(request.getLocation())
                .uploadDate(request.getUploadDate())
                .build();

        createPortfolioPhotos(portfolio, request.getPhotos());

        portfolioRepository.save(portfolio);

        return PortfolioDto.PortfolioResponse.from(portfolio);
    }

    /**
     * 포트폴리오 개별 조회
     * @param portfolioId 포트폴리오 아이디
     * @return 포트폴리오 개별 조회 결과
     */
    public PortfolioDto.PortfolioResponse getPortfolio(Long memberId, Long portfolioId) {
        Portfolio portfolio = portfolioRepository.findPortfolioByIdWithPortfolioPhotos(portfolioId)
                .orElseThrow(() -> ExceptionFactory.of(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));
        long scrapCount = scrapRepository.countByPortfolioId(portfolioId);
        boolean scrapYN = scrapRepository.existsByMemberIdAndPortfolioId(memberId, portfolioId);

        return PortfolioDto.PortfolioResponse.of(portfolio, scrapCount, scrapYN);
    }

    @Transactional(readOnly = true)
    public PortfolioDto.PortfolioListResponse getPortfoliosByPhotographer(Long photographerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Portfolio> portfolioPage = portfolioRepository.findByPhotographerIdOrderByUploadDateDesc(photographerId, pageable);

        List<PortfolioDto.PortfolioSummaryResponse> summaries = portfolioPage.getContent().stream()
                .map(portfolio -> {
                    String representativeImage = portfolio.getPortfolioPhotos().stream()
                            .min(Comparator.comparingInt(PortfolioPhoto::getPhotoOrder))
                            .map(photo -> s3PresignedUrlService.generateDownloadUrl(photo.getImage()).toString())
                            .orElse(null);
                    return PortfolioDto.PortfolioSummaryResponse.of(portfolio, representativeImage);
                })
                .toList();

        return PortfolioDto.PortfolioListResponse.of(summaries, portfolioPage.getTotalElements(), portfolioPage.getTotalPages(), page);
    }

    /**
     * 포트폴리오 사진을 1:N 테이블에 저장
     * @param portfolio 포트폴리오 정보
     * @param photoDtos 포트폴리오 사진 정보
     */
    private void createPortfolioPhotos(Portfolio portfolio, List<PortfolioDto.PortfolioPhotoRequest> photoDtos) {
        List<PortfolioPhoto> photos = new ArrayList<>();

        for(PortfolioDto.PortfolioPhotoRequest photoRequest : photoDtos) {
            PortfolioPhoto portfolioPhoto = PortfolioPhoto.builder()
                    .portfolio(portfolio)
                    .image(photoRequest.getImage())
                    .photoOrder(photoRequest.getPhotoOrder())
                    .build();
            photos.add(portfolioPhoto);
        }

        portfolio.addAllPortfolioPhotos(photos);
    }
}
