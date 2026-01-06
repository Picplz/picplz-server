package com.hm.picplz.domain.review.service;

import com.hm.picplz.domain.customer.domain.Customer;
import com.hm.picplz.domain.customer.repository.CustomerRepository;
import com.hm.picplz.domain.member.domain.Member;
import com.hm.picplz.domain.member.domain.Role;
import com.hm.picplz.domain.member.repository.MemberRepository;
import com.hm.picplz.domain.photographer.domain.Photographer;
import com.hm.picplz.domain.photographer.repository.PhotographerRepository;
import com.hm.picplz.domain.review.domain.Review;
import com.hm.picplz.domain.review.domain.ReviewPhoto;
import com.hm.picplz.domain.review.dto.*;
import com.hm.picplz.domain.review.exception.ReviewErrorCode;
import com.hm.picplz.domain.review.repository.ReviewPhotoRepository;
import com.hm.picplz.domain.review.repository.ReviewRepository;
import com.hm.picplz.global.error.BaseErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewPhotoRepository reviewPhotoRepository;
    private final PhotographerRepository photographerRepository;
    private final CustomerRepository customerRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional(readOnly = true)
    public ReviewListResponse getReviewsByPhotographer(Long photographerId, Pageable pageable) {

        // 작가 유무 검증
        photographerRepository.findById(photographerId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.PHOTOGRAPHER_NOT_FOUND));

        Page<Review> reviewPage = reviewRepository.findByPhotographerId(photographerId, pageable);

        // 리뷰 목록
        List<Long> reviewIds = new ArrayList<>();

        for (Review review : reviewPage.getContent()) {
            reviewIds.add(review.getId());
        }

        // 리뷰 사진 일괄 조회
        List<ReviewPhoto> photos = reviewPhotoRepository.findByReviewIdIn(reviewIds);

//        Map<Long, List<ReviewPhoto>> photosByReviewId = photos.stream()
//                .collect(Collectors.groupingBy(photo -> photo.getReview().getId()));
        Map<Long, List<ReviewPhoto>> photosByReviewId = new HashMap<>();

        for (ReviewPhoto photo : photos) {
            Long reviewId = photo.getReview().getId();

            if (!photosByReviewId.containsKey(reviewId)) {
                photosByReviewId.put(reviewId, new ArrayList<>());
            }

            photosByReviewId.get(reviewId).add(photo);
        }


        // Dto 생성
        List<ReviewSummary> reviewSummaries = new ArrayList<>();

        for (Review review : reviewPage.getContent()) {
            Long reviewId = review.getId();

            List<ReviewPhoto> reviewPhotos =
                    photosByReviewId.getOrDefault(reviewId, new ArrayList<>());

            ReviewSummary summary =
                    ReviewSummary.from(review, reviewPhotos);

            reviewSummaries.add(summary);
        }


        // 작가 별점 평균, 리뷰 개수
        Float averageRating = reviewRepository.findAverageRatingByPhotographerId(photographerId)
                .orElse(0.0f);
        Long totalReviews = reviewRepository.countByPhotographerId(photographerId);

        return ReviewListResponse.builder()
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .reviews(reviewSummaries)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PhotographerRatingResponse getPhotographerRating(Long photographerId) {
        // 작가 유무 검증
        photographerRepository.findById(photographerId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.PHOTOGRAPHER_NOT_FOUND));

        Float averageRating = reviewRepository.findAverageRatingByPhotographerId(photographerId)
                .orElse(0.0f);
        Long totalReviews = reviewRepository.countByPhotographerId(photographerId);

        return PhotographerRatingResponse.builder()
                .averageRating(averageRating)
                .totalReviews(totalReviews)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ReviewDetailResponse getReviewDetail(Long reviewId) {
        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.REVIEW_NOT_FOUND));

        List<ReviewPhoto> photos = reviewPhotoRepository.findByReviewIdOrderByPhotoOrder(reviewId);

        return ReviewDetailResponse.from(review, photos);
    }

    @Override
    @Transactional
    public ReviewDetailResponse createReview(Long memberId, CreateReviewRequest request) {

        // 멤버 유무 검증
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.CUSTOMER_NOT_FOUND));
        // 작가는 리뷰 작성 불가능? 굳이 필요한지는 모르겠습니다.
        if (member.getRole() != Role.CUSTOMER) {
            throw new BaseErrorException(ReviewErrorCode.ONLY_CUSTOMER_CAN_WRITE_REVIEW);
        }

        // 해당 고객 유무 검증
        Customer customer = customerRepository.findByMemberId(memberId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.CUSTOMER_NOT_FOUND));

        // 해당 작가 검증
        Photographer photographer = photographerRepository.findById(request.getPhotographerId())
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.PHOTOGRAPHER_NOT_FOUND));

        // 리뷰 조건 검증
        validateRating(request.getRating());
        validateReviewContent(request.getRating(), request.getContent());
        validateReviewPhotos(request.getPhotoUrls());
        
        Review review = request.toEntity(photographer, customer);
        review = reviewRepository.save(review);

        List<ReviewPhoto> photos = new ArrayList<>();
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            for (int i = 0; i < request.getPhotoUrls().size(); i++) {
                ReviewPhoto photo = ReviewPhoto.of(review, request.getPhotoUrls().get(i), i);
                photos.add(reviewPhotoRepository.save(photo));
            }
        }

        return ReviewDetailResponse.from(review, photos);
    }

    @Override
    @Transactional
    public ReviewDetailResponse updateReview(Long memberId, Long reviewId, UpdateReviewRequest request) {

        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 고객 자신이 쓴 리뷰인지 검증
        if (!review.getCustomer().getMember().getId().equals(memberId)) {
            throw new BaseErrorException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }

        // 리뷰 조건 검증
        validateRating(request.getRating());
        validateReviewContent(request.getRating(), request.getContent());
        validateReviewPhotos(request.getPhotoUrls());

        review.updateReview(request.getRating(), request.getContent());

        reviewPhotoRepository.deleteByReviewId(reviewId);

        List<ReviewPhoto> photos = new ArrayList<>();
        if (request.getPhotoUrls() != null && !request.getPhotoUrls().isEmpty()) {
            for (int i = 0; i < request.getPhotoUrls().size(); i++) {
                ReviewPhoto photo = ReviewPhoto.of(review, request.getPhotoUrls().get(i), i);
                photos.add(reviewPhotoRepository.save(photo));
            }
        }

        return ReviewDetailResponse.from(review, photos);
    }

    @Override
    @Transactional
    public void deleteReview(Long memberId, Long reviewId) {

        Review review = reviewRepository.findByIdWithDetails(reviewId)
                .orElseThrow(() -> new BaseErrorException(ReviewErrorCode.REVIEW_NOT_FOUND));

        // 고객 자신이 쓴 리뷰인지 검증
        if (!review.getCustomer().getMember().getId().equals(memberId)) {
            throw new BaseErrorException(ReviewErrorCode.UNAUTHORIZED_REVIEW_ACCESS);
        }
        reviewPhotoRepository.deleteByReviewId(reviewId);

        reviewRepository.delete(review);
    }

    // 별점은 1~5까지
    private void validateRating(Float rating) {
        if (rating == null || rating < 1.0f || rating > 5.0f) {
            throw new BaseErrorException(ReviewErrorCode.INVALID_RATING);
        }
    }

    private void validateReviewContent(Float rating, String content) {
        // 별점 3~5: 리뷰 내용 최소 10자 이상 필수
        if (rating >= 3.0f) {
            if (content == null || content.trim().length() < 10) {
                throw new BaseErrorException(ReviewErrorCode.INVALID_REVIEW_CONTENT);
            }
        }
    }

    // 사진 업로드는 10장 이하로 제한
    private void validateReviewPhotos(List<String> photoUrls) {
        if (photoUrls != null && photoUrls.size() > 10) {
            throw new BaseErrorException(ReviewErrorCode.TOO_MANY_PHOTOS);
        }
    }
}
