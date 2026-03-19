package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.enums.Gender;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import jakarta.validation.constraints.*;

import java.time.LocalDate;

@Data
public class SignUpRequestDto {

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Name is required")
    @Size(min = 3, max = 30, message = "Name must be between 3 and 30 characters")
    private String name;

    private LocalDate dateOfBirth;

    @NotNull(message = "Gender is required")
    private Gender gender;
}

