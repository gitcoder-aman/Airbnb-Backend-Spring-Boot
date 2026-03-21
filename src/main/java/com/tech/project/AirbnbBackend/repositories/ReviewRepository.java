package com.tech.project.AirbnbBackend.repositories;


import com.tech.project.AirbnbBackend.entities.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ReviewRepository extends JpaRepository<Review,Long>, JpaSpecificationExecutor<Review> {

    Page<Review> findByRoomId(Long roomId, Pageable pageable);

    Page<Review> findByRoomIdAndPhotosIsNotNull(Long roomId, Pageable pageable);
}
