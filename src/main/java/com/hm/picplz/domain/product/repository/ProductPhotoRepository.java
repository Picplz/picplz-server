package com.hm.picplz.domain.product.repository;

import com.hm.picplz.domain.product.domain.ProductPhoto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductPhotoRepository extends JpaRepository<ProductPhoto, Long> {
    List<ProductPhoto> findByShootProductIdOrderByPhotoOrderAsc(Long shootProductId);
}
