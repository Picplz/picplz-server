package com.hm.picplz.domain.product.dto;

import com.hm.picplz.domain.product.domain.ProductPhoto;
import com.hm.picplz.domain.product.domain.ShootProduct;
import java.util.List;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductDto {

    @Data
    @NoArgsConstructor
    public static class Create {

        private Long photographerId;
        private String name;
        private String description;
        private int shootPrice;
        private int shootDuration;
        private int amount;
        private String editedYn;
        private int editPrice;
        private String otherDetails;
        private List<String> productPhotos;
    }

    @Data
    @NoArgsConstructor
    public static class ProductId {

        private Long id;

        public static ProductId of(Long id) {
            ProductId productId = new ProductId();
            productId.id = id;

            return productId;
        }
    }

    @Data
    @NoArgsConstructor
    public static class Detail {

        private Long productId;
        private Long photographerId;
        private String name;
        private int shootPrice;
        private int shootDuration;
        private int amount;
        private String editedYn;
        private int editPrice;
        private String otherDetails;
        private List<String> productPhotos;

        public static Detail of(ShootProduct product, List<ProductPhoto> photos) {
            Detail detail = new Detail();
            detail.productId = product.getId();
            detail.photographerId = product.getPhotographer().getId();
            detail.name = product.getName();
            detail.shootPrice = product.getShootPrice();
            detail.shootDuration = product.getShootDuration();
            detail.amount = product.getAmount();
            detail.editedYn = product.getEditedYn().getValue();
            detail.editPrice = product.getEditPrice();
            detail.otherDetails = product.getOtherDetails();
            detail.productPhotos = photos.stream()
                    .map(ProductPhoto::getImageData)
                    .toList();

            return detail;
        }
    }
}
