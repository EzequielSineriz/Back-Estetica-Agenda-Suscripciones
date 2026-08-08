package com.AppEstetica.controller;

import com.AppEstetica.service.Pagos.PagoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoWebhookController {
    private final PagoService pagoService;

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String topic,
            @RequestParam(name = "id", required = false) String id,
            @RequestBody(required = false) Map<String, Object> body) {

        String finalTopic = topic;
        String finalPaymentId = id;

        // Formato nuevo: viene en el body, no en query params
        if (finalTopic == null && body != null) {
            Object type = body.get("type");
            finalTopic = type != null ? type.toString() : null;

            Object dataObj = body.get("data");
            if (dataObj instanceof Map<?, ?> dataMap) {
                Object idObj = dataMap.get("id");
                finalPaymentId = idObj != null ? idObj.toString() : null;
            }
        }

        log.info("Webhook recibido - topic: {}, paymentId: {}", finalTopic, finalPaymentId);

        if ("payment".equals(finalTopic) && finalPaymentId != null) {
            try {
                pagoService.procesarNotificacion(finalPaymentId);
            } catch (Exception e) {
                log.error("Error procesando webhook de pago {}: {}", finalPaymentId, e.getMessage());
            }
        }
        return ResponseEntity.ok().build();
    }
}