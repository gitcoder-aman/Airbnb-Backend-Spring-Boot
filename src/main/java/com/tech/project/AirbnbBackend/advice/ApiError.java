package com.tech.project.AirbnbBackend.advice;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class ApiError {
    private HttpStatus httpStatus;
    private String message;
    private Map<String,String> subErrors;
}
