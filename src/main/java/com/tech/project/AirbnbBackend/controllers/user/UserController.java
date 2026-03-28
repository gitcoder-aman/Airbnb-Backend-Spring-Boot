package com.tech.project.AirbnbBackend.controllers.user;

import com.tech.project.AirbnbBackend.dto.ProfileUpdateRequestDto;
import com.tech.project.AirbnbBackend.dto.UserDto;
import com.tech.project.AirbnbBackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PutMapping("/profile")
    public ResponseEntity<Void>updateProfile(@RequestBody ProfileUpdateRequestDto profileUpdateRequestDto){
        userService.updateProfile(profileUpdateRequestDto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto>getMyProfile(){
        return  ResponseEntity.ok(userService.getMyProfile());
    }
}
