package com.hm.picplz.domain.portfolio.service;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.service.PhotographerService;
import com.hm.picplz.domain.portfolio.domain.Portfolio;
import com.hm.picplz.domain.portfolio.domain.PortfolioPhoto;
import com.hm.picplz.domain.portfolio.dto.PortfolioDto;
import com.hm.picplz.domain.portfolio.repository.PortfolioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PhotographerService photographerService;
    private final PortfolioRepository portfolioRepository;

    /**
     * 포트폴리오 생성
     * @param memberId 멤버 아이디
     * @param request 포트폴리오 생성 요청
     * @return 포트폴리오 생성 결과
     */
    @Transactional
    public PortfolioDto.CreatePortfolioResponse create(Long memberId, PortfolioDto.CreatePortfolioRequest request) {
        Photographer photographer = photographerService.getPhotographerByMemberId(memberId);

        Portfolio portfolio = Portfolio.builder()
                .photographer(photographer)
                .location(request.getLocation())
                .uploadDate(request.getUploadDate())
                .build();

        createPortfolioPhotos(portfolio, request.getPhotos());

        portfolioRepository.save(portfolio);

        return PortfolioDto.CreatePortfolioResponse.from(portfolio);
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
