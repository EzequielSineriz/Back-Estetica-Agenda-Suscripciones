package com.AppEstetica.controller;

import com.AppEstetica.service.Pagos.PagoService;
import com.AppEstetica.service.Pagos.WebhookSignatureValidator;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Pagos", description = "Service de los webhook, recepcion de notificaciones")
@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoWebhookController {

    private final PagoService pagoService;
    private final WebhookSignatureValidator signatureValidator;

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String topic,
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataIdParam,
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) Map<String, Object> body) {

        String finalTopic = topic != null ? topic : type;
        String finalPaymentId = id != null ? id : dataIdParam;

        if (finalPaymentId == null && body != null) {
            Object bodyType = body.get("type");
            if (finalTopic == null && bodyType != null) finalTopic = bodyType.toString();

            Object dataObj = body.get("data");
            if (dataObj instanceof Map<?, ?> dataMap) {
                Object idObj = dataMap.get("id");
                finalPaymentId = idObj != null ? idObj.toString() : null;
            }
        }

        log.info("Webhook recibido - topic: {}, paymentId: {}", finalTopic, finalPaymentId);

        if (xSignature != null && finalPaymentId != null) {
            if (!signatureValidator.esValida(xSignature, xRequestId, finalPaymentId)) {
                log.warn("Firma de webhook inválida - posible notificación falsa. paymentId: {}", finalPaymentId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

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