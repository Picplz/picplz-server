package com.hm.picplz.domain.product.exception;

import com.hm.picplz.global.error.BaseErrorException;

public class ProductNotFound extends BaseErrorException {

    public static final ProductNotFound EXCEPTION = new ProductNotFound();

    private ProductNotFound() {
        super(ProductErrorCode.PRODUCT_NOT_FOUND);
    }
}
