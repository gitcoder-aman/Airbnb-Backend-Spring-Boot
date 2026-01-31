package com.tech.project.AirbnbBackend.services.impl;

import com.tech.project.AirbnbBackend.entities.User;
import com.tech.project.AirbnbBackend.exception.ResourceNotFoundException;
import com.tech.project.AirbnbBackend.repositories.UserRepository;
import com.tech.project.AirbnbBackend.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return Objects.requireNonNull(userRepository.findByEmail(username).orElse(null));
    }
}
