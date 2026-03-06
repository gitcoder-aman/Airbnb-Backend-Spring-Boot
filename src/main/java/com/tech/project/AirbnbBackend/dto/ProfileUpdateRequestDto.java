package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.enums.Gender;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileUpdateRequestDto {
    private String name;
    private LocalDate dateOfBirth;
    private Gender gender;
}
