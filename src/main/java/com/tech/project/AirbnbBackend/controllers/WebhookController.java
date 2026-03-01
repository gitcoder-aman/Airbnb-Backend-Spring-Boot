package com.tech.project.AirbnbBackend.controllers;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.tech.project.AirbnbBackend.services.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/webhook")
@RequiredArgsConstructor
public class WebhookController {
//    private final PaymentService paymentService;

    private final BookingService bookingService;

    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    //    @PostMapping("/payment")
//    public ResponseEntity<Void>capturePayments(@RequestBody String payload, @RequestHeader("Stripe-Signature") String sigHeader){
//        try {
//            Event event = Webhook.constructEvent(payload,sigHeader,endpointSecret);
//            bookingService.capturePayment(event);
//            return ResponseEntity.noContent().build();
//
//        }catch (SignatureVerificationException e){
//            throw new RuntimeException(e);
//        }
//    }
    @PostMapping("/payment")
    public ResponseEntity<Void> capturePayments(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
        } catch (SignatureVerificationException e) {
            return ResponseEntity.badRequest().build();
        }

        bookingService.capturePayment(event);

        return ResponseEntity.noContent().build();
    }
}
