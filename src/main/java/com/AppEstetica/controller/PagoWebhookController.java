package com.AppEstetica.controller;

import com.AppEstetica.service.Pagos.PagoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Tag(name = "Pagos", description = "Service de los webhook, recepcion de notificaciones")
@Slf4j
@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoWebhookController {

    private final PagoService pagoService;

    @Value("${mercadopago.webhook-secret}")
    private String webhookSecret;

    @PostMapping("/webhook")
    public ResponseEntity<Void> recibirNotificacion(
            @RequestParam(required = false) String topic,
            @RequestParam(name = "id", required = false) String id,
            @RequestParam(name = "type", required = false) String type,
            @RequestParam(name = "data.id", required = false) String dataIdParam,
            @RequestHeader(name = "x-signature", required = false) String xSignature,
            @RequestHeader(name = "x-request-id", required = false) String xRequestId,
            @RequestBody(required = false) Map<String, Object> body) {

        // 1. Resolver topic/paymentId sin importar qué formato mandó MercadoPago
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

        // 2. Validar firma (si vino x-signature, la exigimos válida; MercadoPago siempre la manda en producción)
        if (xSignature != null && finalPaymentId != null) {
            if (!validarFirma(xSignature, xRequestId, finalPaymentId)) {
                log.warn("Firma de webhook inválida - posible notificación falsa. paymentId: {}", finalPaymentId);
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }

        // 3. Procesar (nunca propagamos excepciones internas, para no generar reintentos innecesarios)
        if ("payment".equals(finalTopic) && finalPaymentId != null) {
            try {
                pagoService.procesarNotificacion(finalPaymentId);
            } catch (Exception e) {
                log.error("Error procesando webhook de pago {}: {}", finalPaymentId, e.getMessage());
            }
        }

        return ResponseEntity.ok().build();
    }

    private boolean validarFirma(String xSignature, String xRequestId, String dataId) {
        try {
            String ts = null;
            String hash = null;

            for (String part : xSignature.split(",")) {
                String[] kv = part.split("=", 2);
                if (kv.length != 2) continue;
                String key = kv[0].trim();
                String value = kv[1].trim();
                if ("ts".equals(key)) ts = value;
                if ("v1".equals(key)) hash = value;
            }

            if (ts == null || hash == null) return false;

            StringBuilder manifest = new StringBuilder();
            manifest.append("id:").append(dataId.toLowerCase()).append(";");
            if (xRequestId != null) {
                manifest.append("request-id:").append(xRequestId).append(";");
            }
            manifest.append("ts:").append(ts).append(";");

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(manifest.toString().getBytes(StandardCharsets.UTF_8));

            StringBuilder computed = new StringBuilder();
            for (byte b : rawHmac) computed.append(String.format("%02x", b));

            return computed.toString().equals(hash);

        } catch (Exception e) {
            log.error("Error validando firma de webhook", e);
            return false;
        }
    }
}