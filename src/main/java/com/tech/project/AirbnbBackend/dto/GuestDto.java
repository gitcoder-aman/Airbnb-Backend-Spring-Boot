package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.entities.enums.Gender;
import lombok.Data;

import jakarta.validation.constraints.*;

@Data
public class GuestDto {

    private Long id;

    @NotNull(message = "User reference is required")
    private User user;

    @NotBlank(message = "Guest name cannot be empty")
    @Size(min = 2, max = 50, message = "Guest name must be between 2 and 50 characters")
    private String name;

    @NotNull(message = "Gender is required")
    private Gender gender;

    @NotNull(message = "Age is required")
    @Min(value = 1, message = "Age must be at least 1")
    @Max(value = 120, message = "Age must be realistic")
    private Integer age;
}
