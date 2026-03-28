package com.tech.project.AirbnbBackend.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReviewResponseDto {

    private Long id;

    private String userName;

    private Long userId;

    private int rating;

    private String comment;

    private String[] photos;

    private boolean verified;

    private LocalDateTime createdAt;
}
