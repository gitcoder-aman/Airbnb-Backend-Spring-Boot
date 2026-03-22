package com.tech.project.AirbnbBackend.controllers.user;

import com.tech.project.AirbnbBackend.advice.ApiResponse;
import com.tech.project.AirbnbBackend.dto.ReviewRequestDto;
import com.tech.project.AirbnbBackend.dto.ReviewResponseDto;
import com.tech.project.AirbnbBackend.services.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    @PreAuthorize("hasRole('GUEST')")
    @PostMapping("/rooms/{roomId}/reviews")
    public ResponseEntity<ApiResponse<ReviewResponseDto>> createReview(
            @Valid @RequestBody ReviewRequestDto reviewRequestDto,
            @PathVariable Long roomId
    ) {

        ReviewResponseDto reviewResponse = reviewService.createReview(roomId, reviewRequestDto);

        return ResponseEntity.ok(new ApiResponse<>(reviewResponse));
    }

    @GetMapping("/reviews")
    public ResponseEntity<ApiResponse<Page<ReviewResponseDto>>> getReviews(
            @RequestParam Long roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction,
            @RequestParam(required = false) Boolean hasPhotos
    ) {

        Page<ReviewResponseDto> reviews = reviewService.getReviews(
                roomId, page, size, sortBy, direction, hasPhotos
        );

        return ResponseEntity.ok(new ApiResponse<>(reviews));
    }

    @DeleteMapping("reviews/{reviewId}")
    public ResponseEntity<ApiResponse<String>>deleteReview(@PathVariable Long reviewId){
        reviewService.deleteReview(reviewId);
        return ResponseEntity.ok(
                new ApiResponse<>("Review successfully deleted!")
        );
    }
    @PutMapping("reviews/{reviewId}")
    public ResponseEntity<ApiResponse<ReviewResponseDto>>updateReview(@PathVariable Long reviewId,@Valid @RequestBody ReviewRequestDto reviewRequestDto){
        ReviewResponseDto reviewResponse = reviewService.updateReview(reviewId,reviewRequestDto);
        return ResponseEntity.ok(new ApiResponse<>(reviewResponse));
    }
    @GetMapping("reviews/has-completed-booking/{roomId}")
    public ResponseEntity<Boolean>hasCompletedBookingForReview(@PathVariable Long roomId){
        Boolean userBookingCompletedForReview = reviewService.isUserBookingCompletedForReview(roomId);
        return ResponseEntity.ok(userBookingCompletedForReview);
    }
}