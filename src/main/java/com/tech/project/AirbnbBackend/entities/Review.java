package com.tech.project.AirbnbBackend.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Table(
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "room_id"})  //one to one (one user can review only one in each room)
)
@Getter
@Setter
@Entity
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id",nullable = false)
    private User user;     // Who gave review

    @ManyToOne
    @JoinColumn(name = "room_id",nullable = false)
    private Room room;     // Which room

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int rating;      // 1 to 5

    @Column(nullable = false)
    private String comment;  // Review text

    @Column(columnDefinition = "TEXT[]")
    private String [] photos;

    private boolean verified;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}