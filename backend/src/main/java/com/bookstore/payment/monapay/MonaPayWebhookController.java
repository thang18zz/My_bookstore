package com.bookstore.payment.monapay;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/payment/monapay")
@RequiredArgsConstructor
public class MonaPayWebhookController {

    private final MonaPayWebhookService webhookService;

    @PostMapping("/webhook")
    public ResponseEntity<String> receiveWebhook(
            @RequestBody(required = false) byte[] rawBody,
            @RequestHeader(value = "X-Mona-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Mona-Signature", required = false) String signature,
            @RequestHeader Map<String, String> allHeaders
    ) {
        log.info("Incoming MonaPay webhook. Payload size: {} bytes", rawBody != null ? rawBody.length : 0);

        if (timestamp == null || timestamp.isBlank()) {
            timestamp = allHeaders.getOrDefault("x-mona-timestamp",
                    allHeaders.getOrDefault("x-monapay-timestamp",
                    allHeaders.get("monapay-timestamp")));
        }

        if (signature == null || signature.isBlank()) {
            signature = allHeaders.getOrDefault("x-mona-signature",
                    allHeaders.getOrDefault("x-monapay-signature",
                    allHeaders.get("monapay-signature")));
        }

        webhookService.handleWebhook(
                rawBody != null ? rawBody : new byte[0],
                timestamp,
                signature);

        return ResponseEntity.ok("OK");
    }
}
