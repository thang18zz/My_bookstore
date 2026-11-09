package com.bookstore.order.dto;

import com.bookstore.order.PaymentMethod;

import jakarta.validation.constraints.NotNull;

import lombok.Getter;
import lombok.Setter;

/**
 * UC4.1.1/4.1.2
 * Checkout with shipping address and payment method.
 */
@Getter
@Setter
public class CheckoutRequest {

    @NotNull
    private Long addressId;

    private PaymentMethod paymentMethod = PaymentMethod.OFFLINE;
}