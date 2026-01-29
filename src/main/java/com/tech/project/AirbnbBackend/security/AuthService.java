package com.tech.project.AirbnbBackend.security;

import com.tech.project.AirbnbBackend.dto.LoginDto;
import com.tech.project.AirbnbBackend.dto.SignUpRequestDto;
import com.tech.project.AirbnbBackend.dto.UserDto;
import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.entities.enums.Role;
import com.tech.project.AirbnbBackend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public UserDto signup(SignUpRequestDto signUpRequestDto) {
        User user = userRepository.findByEmail(signUpRequestDto.getEmail()).orElse(null);
        if (user != null) {
            throw new RuntimeException("User is already present with same email id");
        }
        User newUser = modelMapper.map(signUpRequestDto, User.class);
        newUser.setRoles(Set.of(Role.GUEST));
        newUser.setPassword(passwordEncoder.encode(signUpRequestDto.getPassword()));
        newUser = userRepository.save(newUser);
        return modelMapper.map(newUser, UserDto.class);
    }

    public String[] login(LoginDto loginDto) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginDto.getEmail(),
                loginDto.getPassword()
        ));
        User user = (User) authentication.getPrincipal();
        String[] arr = new String[2];
        assert user != null;
        arr[0] = jwtService.generateAccessToken(user);
        arr[1] = jwtService.generateRefreshToken(user);
        return arr;
    }
}
