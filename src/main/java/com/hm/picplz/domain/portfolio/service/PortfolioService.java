package com.hm.picplz.domain.portfolio.service;

import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.service.MemberService;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.service.PhotographerService;
import com.hm.picplz.domain.portfolio.domain.Portfolio;
import com.hm.picplz.domain.portfolio.domain.PortfolioPhoto;
import com.hm.picplz.domain.portfolio.domain.Scrap;
import com.hm.picplz.domain.portfolio.dto.PortfolioDto;
import com.hm.picplz.domain.portfolio.exception.PortfolioErrorCode;
import com.hm.picplz.domain.portfolio.repository.PortfolioRepository;
import com.hm.picplz.domain.portfolio.repository.ScrapRepository;
import com.hm.picplz.global.error.ExceptionFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PortfolioService {

    private final PhotographerService photographerService;
    private final MemberService memberService;
    private final PortfolioRepository portfolioRepository;
    private final ScrapRepository scrapRepository;

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
        Portfolio portfolio = getPortfolioById(portfolioId);
        long scrapCount = scrapRepository.countByPortfolioId(portfolioId);
        boolean scrapYN = scrapRepository.existsByMemberIdAndPortfolioId(memberId, portfolioId);

        return PortfolioDto.PortfolioResponse.of(portfolio, scrapCount, scrapYN);
    }

    /**
     * 스크랩 저장
     * @param memberId 스크랩 하려는 멤버의 ID
     * @param portfolioId 스크랩 하려는 포트폴리오의 ID
     */
    @Transactional
    public void createScrap(Long memberId, Long portfolioId) {
        if(scrapRepository.existsByMemberIdAndPortfolioId(memberId, portfolioId)) {
            throw ExceptionFactory.of(PortfolioErrorCode.SCRAP_ALREADY_EXISTS);
        }

        Portfolio portfolio = getPortfolioById(portfolioId);
        Member member = memberService.getMemberById(memberId);

        Scrap scrap = Scrap.builder()
                .portfolio(portfolio)
                .member(member)
                .build();

        portfolio.addScrap(scrap);
    }

    /**
     * 스크랩 삭제
     * @param memberId 삭제하려는 스크랩의 멤버 ID
     * @param portfolioId 삭제하려는 스크랩의 포트폴리오 ID
     */
    @Transactional
    public void deleteScrap(Long memberId, Long portfolioId) {
        Scrap scrap = scrapRepository.findByMemberIdAndPortfolioId(memberId, portfolioId)
                .orElseThrow(() -> ExceptionFactory.of(PortfolioErrorCode.SCRAP_NOT_FOUND));
        scrapRepository.delete(scrap);
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

    private Portfolio getPortfolioById(Long portfolioId) {
        return portfolioRepository.findById(portfolioId)
                .orElseThrow(() -> ExceptionFactory.of(PortfolioErrorCode.PORTFOLIO_NOT_FOUND));
    }
}
