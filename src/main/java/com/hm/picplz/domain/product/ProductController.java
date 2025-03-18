package com.hm.picplz.domain.product;

import com.hm.picplz.domain.product.dto.ProductDto;
import com.hm.picplz.domain.product.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/products")
@Tag(name = "Product")
public class ProductController {

    private final ProductService productService;

    @Operation(summary = "촬영 상품 생성")
    @PostMapping
    public ProductDto.ProductId createProduct(
            @RequestBody ProductDto.Create createProductRequest
    ) {
        log.info("촬영 상품 생성하기");
        return productService.createProduct(createProductRequest);
    }

    @Operation(summary = "촬영 상품 상세 조회")
    @GetMapping("/{productId}")
    public ProductDto.Detail loadProductDetailById(
            @PathVariable(name = "productId") Long productId
    ) {
        log.info("촬영 상품 상세 조회하기");
        return productService.findProductDetailById(productId);
    }

}
