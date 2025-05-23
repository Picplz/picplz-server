package com.hm.picplz.domain.product.service;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.exception.PhotographerErrorCode;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.domain.product.domain.ProductPhoto;
import com.hm.picplz.domain.product.domain.ShootProduct;
import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.domain.product.repository.ProductPhotoRepository;
import com.hm.picplz.domain.product.repository.ProductRepository;
import com.hm.picplz.global.error.ExceptionFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductPhotoRepository productPhotoRepository;
    private final PhotographerRepository photographerRepository;

    @Transactional
    public ProductDto.ProductId createProduct(ProductDto.Create request) {
        Photographer photographer = photographerRepository.findByMemberId(request.getPhotographerId())
            .orElseThrow(() ->  ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
        ShootProduct product = ShootProduct.of(request, photographer);
        ShootProduct savedProduct = productRepository.save(product);

        IntStream.range(0, request.getProductPhotos().size())
                .mapToObj(i -> ProductPhoto.of(request.getProductPhotos().get(i), i+1, savedProduct))
                .forEach(productPhotoRepository::save);
        return ProductDto.ProductId.of(savedProduct.getId());
    }

    public ProductDto.Detail findProductDetailById(Long productId) {
        ShootProduct product = productRepository.findById(productId)
            .orElseThrow(() ->  ExceptionFactory.of(PhotographerErrorCode.PHOTOGRAPHER_NOT_FOUND));
        List<ProductPhoto> photos = productPhotoRepository.findByShootProductIdOrderByPhotoOrderAsc(productId);
        return ProductDto.Detail.of(product, photos);
    }

    public List<ProductDto.Detail> findProductsByPhotographer(Long photographerId) {
        List<ShootProduct> products = productRepository.findByPhotographerId(photographerId);
        return products.stream()
                .map(product -> {
                    List<ProductPhoto> photos = productPhotoRepository.findByShootProductIdOrderByPhotoOrderAsc(product.getId());
                    return ProductDto.Detail.of(product, photos);
                })
                .toList();
    }
}
