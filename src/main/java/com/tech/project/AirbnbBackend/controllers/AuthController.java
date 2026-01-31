package com.tech.project.AirbnbBackend.controllers;

import com.tech.project.AirbnbBackend.dto.LoginDto;
import com.tech.project.AirbnbBackend.dto.LoginResponseDto;
import com.tech.project.AirbnbBackend.dto.SignUpRequestDto;
import com.tech.project.AirbnbBackend.dto.UserDto;
import com.tech.project.AirbnbBackend.security.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/signup")
    public ResponseEntity<UserDto>signup(@Valid @RequestBody SignUpRequestDto signUpRequestDto){
        return new ResponseEntity<>(authService.signup(signUpRequestDto), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto>login(@Valid @RequestBody LoginDto loginDto, HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse){

        String [] token = authService.login(loginDto);
        Cookie cookie = new Cookie("refreshToken",token[1]);
        cookie.setHttpOnly(true);
        httpServletResponse.addCookie(cookie);

        return new ResponseEntity<>(new LoginResponseDto(token[0]),HttpStatus.OK);
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponseDto>refresh(HttpServletRequest request){
        String refreshToken = Arrays.stream(request.getCookies())
                .filter(cookie->"refreshToken".equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(()->new AuthenticationServiceException("Refresh token not found insides the Cookies"));
        String accessToken = authService.refreshToken(refreshToken);
        return ResponseEntity.ok(new LoginResponseDto(accessToken));
    }

}
