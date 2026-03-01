package com.tech.project.AirbnbBackend.services;

import com.tech.project.AirbnbBackend.entities.Booking;

public interface CheckoutService {

    String getCheckOutSession(Booking booking, String successUrl, String failureUrl);
}
