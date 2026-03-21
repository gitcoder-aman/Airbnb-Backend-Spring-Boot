package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.dto.ReviewRequestDto;
import com.tech.project.AirbnbBackend.dto.ReviewResponseDto;
import com.tech.project.AirbnbBackend.entities.Review;
import com.tech.project.AirbnbBackend.entities.Room;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.entities.enums.BookingStatus;
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

        User user = getCurrentUser();   // logged-in user

        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Room not found"));

        //  Check if user completed booking
        boolean hasCompletedBooking = bookingRepository
                .existsByUserAndRoomAndBookingStatus(user, room, BookingStatus.COMPLETED);

        // Map DTO → Entity
        Review review = modelMapper.map(reviewRequestDto, Review.class);
        review.setUser(user);
        review.setRoom(room);

        //  Set verified flag
        review.setVerified(hasCompletedBooking);

        Review savedReview = reviewRepository.save(review);

        // Convert to DTO
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
