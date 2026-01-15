package com.tech.project.AirbnbBackend.entities;

import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@Embeddable
@ToString
public class HotelContactInfo {
    private String address;
    @NotBlank(message = "Phone number is required")
    @Size(max = 12,min = 10)
    @Pattern(regexp = "\\d+", message = "Phone number must contain only digits.")
    private String phoneNumber;
    @NotBlank(message = "Hotel admin email can't be empty.")
    @Email(message = "Invalid email format")
    private String email;
    @NotBlank(message = "Hotel location must be required.")
    private String location;
}
