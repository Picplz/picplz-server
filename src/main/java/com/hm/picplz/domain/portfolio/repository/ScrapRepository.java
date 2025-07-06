package com.hm.picplz.domain.portfolio.repository;

import com.hm.picplz.domain.portfolio.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    long countByPortfolioId(Long portfolioId);

    boolean existsByMemberIdAndPortfolioId(Long memberId, Long portfolioId);

    Optional<Scrap> findByMemberIdAndPortfolioId(Long memberId, Long portfolioId);
}
