package com.hm.picplz.domain.portfolio.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hm.picplz.domain.portfolio.domain.Portfolio;
import com.hm.picplz.domain.portfolio.domain.PortfolioPhoto;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class PortfolioDto {

    @Data
    @NoArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class PortfolioResponse {
        private Long portfolioId;
        private List<PortfolioPhotoResponse> photos;
        private String location;
        private LocalDate uploadDate;
        private Long scrapCount;
        private Boolean scrapYN;

        public static PortfolioResponse from(Portfolio portfolio) {
            PortfolioResponse response = new PortfolioResponse();
            response.portfolioId = portfolio.getId();
            response.photos = portfolio.getPortfolioPhotos().stream()
                    .map(PortfolioPhotoResponse::from)
                    .sorted(Comparator.comparingInt(PortfolioPhotoResponse::getPhotoOrder))
                    .toList();
            response.location = portfolio.getLocation();
            response.uploadDate = portfolio.getUploadDate();
            return response;
        }

        public static PortfolioResponse of(Portfolio portfolio, Long scrapCount, Boolean scrapYN) {
            PortfolioResponse response = from(portfolio);
            response.scrapCount = scrapCount;
            response.scrapYN = scrapYN;
            return response;
        }
    }

    @Data
    @NoArgsConstructor
    public static class PortfolioSummaryResponse {
        private Long portfolioId;
        private String representativeImage;
        private int photoCount;
        private String location;
        private LocalDate uploadDate;

        public static PortfolioSummaryResponse of(Portfolio portfolio, String representativeImage) {
            PortfolioSummaryResponse response = new PortfolioSummaryResponse();
            response.portfolioId = portfolio.getId();
            response.representativeImage = representativeImage;
            response.photoCount = portfolio.getPortfolioPhotos().size();
            response.location = portfolio.getLocation();
            response.uploadDate = portfolio.getUploadDate();
            return response;
        }
    }

    @Data
    @NoArgsConstructor
    public static class PortfolioListResponse {
        private List<PortfolioSummaryResponse> portfolios;
        private long totalCount;
        private int totalPages;
        private int currentPage;

        public static PortfolioListResponse of(List<PortfolioSummaryResponse> portfolios, long totalCount, int totalPages, int currentPage) {
            PortfolioListResponse response = new PortfolioListResponse();
            response.portfolios = portfolios;
            response.totalCount = totalCount;
            response.totalPages = totalPages;
            response.currentPage = currentPage;
            return response;
        }
    }

    @Data
    @NoArgsConstructor
    public static class CreatePortfolioRequest {
        @Schema(description = "포트폴리오 사진")
        private List<PortfolioPhotoRequest> photos;
        @Schema(description = "촬영 장소")
        private String location;
        @Schema(description = "촬영 날짜", example = "2025-06-24")
        private LocalDate uploadDate;
    }

    @Data
    @NoArgsConstructor
    public static class PortfolioPhotoRequest {
        @Schema(description = "S3 ObjectKey")
        private String image;
        @Schema(description = "사진 순서")
        private Integer photoOrder;
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
