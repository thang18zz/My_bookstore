package com.bookstore.payment.monapay;

import lombok.Getter;
import lombok.Setter;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "monapay")
public class MonaPayConfig {

    private String baseUrl;

    private String clientId;

    private String clientSecret;

    private String webhookSecret;

    private boolean sandbox = true;

    private int expiresIn = 900;

    private String returnUrl;

    private String cancelUrl;
}