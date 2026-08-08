package com.AppEstetica.controller;

import com.AppEstetica.service.Pagos.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoWebhookController {
    private final PagoService pagoService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String topic,
            @RequestParam(name = "id", required = false) String paymentId) {

        if ("payment".equals(topic) && paymentId != null) {
            try {
                pagoService.procesarNotificacion(paymentId);
            } catch (Exception e) {
                log.error("Error procesando webhook de pago {}: {}", paymentId, e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }
}