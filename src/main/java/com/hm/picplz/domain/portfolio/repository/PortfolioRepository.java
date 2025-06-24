package com.hm.picplz.domain.portfolio.repository;

import com.hm.picplz.domain.portfolio.domain.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
}
