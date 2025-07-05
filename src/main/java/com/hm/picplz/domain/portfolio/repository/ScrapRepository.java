package com.hm.picplz.domain.portfolio.repository;

import com.hm.picplz.domain.portfolio.domain.Scrap;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrapRepository extends JpaRepository<Scrap, Long> {

    long countByPortfolioId(Long portfolioId);
}
