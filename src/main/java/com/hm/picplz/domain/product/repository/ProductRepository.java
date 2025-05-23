package com.hm.picplz.domain.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.product.domain.ShootProduct;

public interface ProductRepository extends JpaRepository<ShootProduct, Long> {
    List<ShootProduct> findByPhotographerId(Long photographerId);
}
