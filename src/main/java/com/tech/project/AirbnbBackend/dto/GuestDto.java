package com.tech.project.AirbnbBackend.dto;

import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.entities.enums.Gender;
import lombok.Data;

@Data
public class GuestDto {

    private Long id;
    private User user;

    private String name;

    private Gender gender;

    private Integer age;
}
