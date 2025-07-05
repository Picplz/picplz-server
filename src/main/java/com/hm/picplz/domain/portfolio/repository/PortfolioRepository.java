package com.hm.picplz.domain.portfolio.repository;

import com.hm.picplz.domain.portfolio.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.portfolioPhotos WHERE p.id = :portfolioId")
    Optional<Portfolio> findPortfolioByIdWithPortfolioPhotos(Long portfolioId);
}
