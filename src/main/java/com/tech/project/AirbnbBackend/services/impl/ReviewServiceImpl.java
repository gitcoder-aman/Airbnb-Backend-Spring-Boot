package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.ReviewRequestDto;
import com.tech.project.AirbnbBackend.dto.ReviewResponseDto;
import com.tech.project.AirbnbBackend.entities.Review;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.entities.enums.BookingStatus;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.repositories.BookingRepository;
import com.tech.project.AirbnbBackend.repositories.ReviewRepository;
import com.tech.project.AirbnbBackend.repositories.RoomRepository;
import com.tech.project.AirbnbBackend.security.AuthService;
import com.tech.project.AirbnbBackend.services.ReviewService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import javax.sound.sampled.ReverbType;

import static com.tech.project.AirbnbBackend.utils.AppUtils.getCurrentUser;

@Service
@Slf4j
@RequiredArgsConstructor
public class ReviewServiceImpl implements ReviewService {

    private final ReviewRepository reviewRepository;
    private final ModelMapper modelMapper;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;


    @Override
    public ReviewResponseDto createReview(Long roomId, ReviewRequestDto reviewRequestDto) {

        User user = getCurrentUser();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        //  Check already reviewed
        boolean alreadyReviewed = reviewRepository.existsByUserAndRoom(user, room);

        if (alreadyReviewed) {
            throw new RuntimeException("You have already reviewed this room");
        }

        //  Check booking completed
        Boolean hasCompletedBooking = isUserBookingCompletedForReview(roomId);

        if (!hasCompletedBooking) {
            throw new RuntimeException("You can only review after completing your stay");
        }

        Review review = modelMapper.map(reviewRequestDto, Review.class);
        review.setUser(user);
        review.setRoom(room);
        review.setVerified(true);

        Review savedReview = reviewRepository.save(review);

        ReviewResponseDto response = modelMapper.map(savedReview, ReviewResponseDto.class);
        response.setUserName(user.getName());

        return response;
    }

    @Override
    public Page<ReviewResponseDto> getReviews(Long roomId, int page, int size, String sortBy, String direction, Boolean hasPhotos) {
        Sort sort = direction.equalsIgnoreCase("desc") ?
                Sort.by(sortBy).descending() :
                Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Review> spec = ReviewSpecification.filterReviews(roomId, hasPhotos);

        Page<Review> reviews = reviewRepository.findAll(spec, pageable);

        return reviews.map(review -> {
            ReviewResponseDto dto = modelMapper.map(review, ReviewResponseDto.class);
            dto.setUserName(review.getUser().getName());
            return dto;
        });
    }

    @Override
    public void deleteReview(Long reviewId) {

        reviewRepository.findById(reviewId)
                .orElseThrow(()->new ResourceNotFoundException("Review not found"));

        reviewRepository.deleteById(reviewId);
    }

    @Override
    public ReviewResponseDto updateReview(Long reviewId, ReviewRequestDto reviewRequestDto) {

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(()->new ResourceNotFoundException("Review not found"));

        review.setComment(reviewRequestDto.getComment());
        review.setPhotos(reviewRequestDto.getPhotos());
        review.setRating(reviewRequestDto.getRating());

        review = reviewRepository.save(review);
        return modelMapper.map(review,ReviewResponseDto.class);
    }

    @Override
    public Boolean isUserBookingCompletedForReview(Long roomId) {

        User user = getCurrentUser();

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));
        //  Check booking completed
        return bookingRepository
                .existsByUserAndRoomAndBookingStatus(user, room, BookingStatus.COMPLETED);
    }

    public static class ReviewSpecification {

        public static Specification<Review> filterReviews(Long roomId, Boolean hasPhotos) {
            return (root, query, cb) -> {

                Predicate predicate = cb.equal(root.get("room").get("id"), roomId);

                if (hasPhotos != null && hasPhotos) {
                    predicate = cb.and(predicate, cb.isNotNull(root.get("photos")));
                }

                return predicate;
            };
        }
    }
}
