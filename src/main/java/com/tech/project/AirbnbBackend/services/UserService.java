package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.dto.ProfileUpdateRequestDto;
import com.tech.project.AirbnbBackend.dto.UserDto;
import com.tech.project.AirbnbBackend.entities.User;

public interface UserService {

    User getUserById(Long id);

    void updateProfile(ProfileUpdateRequestDto profileUpdateRequestDto);

    UserDto getMyProfile();
}
