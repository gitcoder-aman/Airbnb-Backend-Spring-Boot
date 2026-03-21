package com.tech.project.AirbnbBackend.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewRequestDto {

//    @NotNull(message = "Room ID is required")
//    private Long roomId;

    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating must be at most 5")
    private int rating;

    @NotBlank(message = "Comment cannot be empty")
    private String comment;

    @Size(max = 5, message = "Maximum 5 photos allowed")
    private String[] photos;
}
