package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.enums.Gender;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String name;
    private String email;
    private String dateOfBirth;
    private Gender gender;
}
