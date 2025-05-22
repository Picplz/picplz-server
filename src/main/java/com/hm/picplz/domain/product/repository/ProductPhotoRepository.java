package com.hm.picplz.domain.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hm.picplz.domain.product.domain.ProductPhoto;

public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {
    List<ProductPhoto> findByShootProductIdOrderByPhotoOrderAsc(Long shootProductId);
}
