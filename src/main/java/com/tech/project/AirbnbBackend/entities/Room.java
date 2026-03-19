package com.tech.project.AirbnbBackend.entities;

import com.tech.project.AirbnbBackend.entities.enums.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"hotel_id", "type"})
        }
)
@Entity
@Getter
@Setter
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "hotel_id",nullable = false)
    private Hotel hotel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RoomType type;

    @Column(nullable = false,precision = 10,scale = 2)
    private BigDecimal basePrice;

    @Column(columnDefinition = "TEXT[]")
    private String[] photos;

    @Column(columnDefinition = "TEXT[]")
    private String[] amenities;

    @Column(nullable = false)
    private Integer totalCount;

    @Column(nullable = false)
    private Integer capacity;

    @Column(nullable = false)
    private String description;


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
