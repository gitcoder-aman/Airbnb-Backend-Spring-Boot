package com.tech.project.AirbnbBackend.utils;

import com.tech.project.AirbnbBackend.entities.User;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Objects;

public class AppUtils {
    public static User getCurrentUser() {

        return (User) Objects.requireNonNull(SecurityContextHolder.getContext().getAuthentication()).getPrincipal();
    }
}
