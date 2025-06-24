package com.hm.picplz.domain.portfolio.dto;

import com.hm.picplz.domain.portfolio.domain.Portfolio;
import com.hm.picplz.domain.portfolio.domain.PortfolioPhoto;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class PortfolioDto {

    @Data
    @NoArgsConstructor
    public static class CreatePortfolioRequest {
        private List<PortfolioPhotoRequest> photos;
        private String location;
        private LocalDate uploadDate;
    }

    @Data
    @NoArgsConstructor
    public static class PortfolioPhotoRequest {
        private String image;
        private Integer photoOrder;
    }

    @Data
    @NoArgsConstructor
    public static class CreatePortfolioResponse {
        private Long portfolioId;
        private List<PortfolioPhotoResponse> photos;
        private String location;
        private LocalDate uploadDate;

        public static CreatePortfolioResponse from(Portfolio portfolio) {
            CreatePortfolioResponse response = new CreatePortfolioResponse();
            response.portfolioId = portfolio.getId();
            response.photos = portfolio.getPortfolioPhotos().stream()
                    .map(PortfolioPhotoResponse::from)
                    .sorted(Comparator.comparingInt(PortfolioPhotoResponse::getPhotoOrder))
                    .toList();
            response.location = portfolio.getLocation();
            response.uploadDate = portfolio.getUploadDate();
            return response;
        }
    }

    @Data
    @NoArgsConstructor
    public static class PortfolioPhotoResponse {
        private Long portfolioPhotoId;
        private String image;
        private Integer photoOrder;

        public static PortfolioPhotoResponse from(PortfolioPhoto photo) {
            PortfolioPhotoResponse response = new PortfolioPhotoResponse();
            response.portfolioPhotoId = photo.getId();
            response.image = photo.getImage();
            response.photoOrder = photo.getPhotoOrder();
            return response;
        }
    }
}
