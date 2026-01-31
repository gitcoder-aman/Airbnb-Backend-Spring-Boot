package com.tech.project.AirbnbBackend.exception;

public class UnAuthorisedException extends RuntimeException{

    public UnAuthorisedException(String message){
        super(message);
    }
}
