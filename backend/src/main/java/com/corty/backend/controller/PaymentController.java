package com.corty.backend.controller;

import com.corty.backend.dto.PaymentIntentRequest;
import com.corty.backend.dto.PaymentIntentResponse;
import com.corty.backend.dto.SavedCardResponse;
import com.corty.backend.dto.SetupIntentResponse;
import com.corty.backend.services.StripeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final StripeService stripeService;

    @PostMapping("/intent")
    public ResponseEntity<PaymentIntentResponse> createIntent(
            @Valid @RequestBody PaymentIntentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(
                stripeService.createPaymentIntent(request.getBookingId(), userDetails.getUsername()));
    }

    @PostMapping("/setup-intent")
    public ResponseEntity<SetupIntentResponse> createSetupIntent(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(stripeService.createSetupIntent(userDetails.getUsername()));
    }

    @GetMapping("/payment-method")
    public ResponseEntity<SavedCardResponse> getSavedCard(
            @AuthenticationPrincipal UserDetails userDetails) {
        SavedCardResponse card = stripeService.getSavedCard(userDetails.getUsername());
        return card != null ? ResponseEntity.ok(card) : ResponseEntity.noContent().build();
    }

    @PostMapping("/payment-method")
    public ResponseEntity<Void> savePaymentMethod(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal UserDetails userDetails) {
        stripeService.savePaymentMethod(userDetails.getUsername(), body.get("paymentMethodId"));
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/payment-method")
    public ResponseEntity<Void> deletePaymentMethod(
            @AuthenticationPrincipal UserDetails userDetails) {
        stripeService.deletePaymentMethod(userDetails.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/webhook")
    public ResponseEntity<Void> webhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {
        stripeService.handleWebhook(payload, sigHeader);
        return ResponseEntity.ok().build();
    }
}
