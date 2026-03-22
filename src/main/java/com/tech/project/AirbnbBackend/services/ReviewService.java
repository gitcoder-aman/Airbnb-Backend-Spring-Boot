package com.tech.project.AirbnbBackend.services;


import com.tech.project.AirbnbBackend.dto.ReviewRequestDto;
import com.tech.project.AirbnbBackend.dto.ReviewResponseDto;
import org.springframework.data.domain.Page;

public interface ReviewService {

    ReviewResponseDto createReview(Long roomId,ReviewRequestDto reviewRequestDto);

    Page<ReviewResponseDto> getReviews(
            Long roomId,
            int page,
            int size,
            String sortBy,
            String direction,
            Boolean hasPhotos
    );
    void deleteReview(Long reviewId);

    ReviewResponseDto updateReview(Long reviewId,ReviewRequestDto reviewRequestDto);

    Boolean isUserBookingCompletedForReview(Long roomId);
}
