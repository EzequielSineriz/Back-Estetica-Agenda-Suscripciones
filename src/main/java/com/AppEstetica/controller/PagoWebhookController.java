package com.AppEstetica.controller;

import com.AppEstetica.service.Pagos.PagoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
            pagoService.procesarNotificacion(paymentId);
        }
        return ResponseEntity.ok().build(); // siempre 200, o MercadoPago reintenta indefinidamente
    }
}