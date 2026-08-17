package com.tech.project.AirbnbBackend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tech.project.AirbnbBackend.entities.enums.Gender;
import com.tech.project.AirbnbBackend.entities.enums.Role;
import lombok.Data;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

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

    @JsonProperty("roles")
    private Set<Role> roles;

    @JsonProperty("role")
    public void setSingleRole(Role role) {
        if (role != null) {
            if (this.roles == null) {
                this.roles = new HashSet<>();
            }
            this.roles.add(role);
        }
    }
}
