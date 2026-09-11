package com.AppEstetica.service.Pagos;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
public class WebhookSignatureValidator {

    private final String webhookSecret;

    public WebhookSignatureValidator(@Value("${mercadopago.webhook-secret}") String webhookSecret) {
        // .trim() por las dudas: un espacio o salto de línea de más al pegar la key en Render
        // rompe el HMAC en silencio y es muy difícil de detectar a simple vista.
        this.webhookSecret = webhookSecret == null ? null : webhookSecret.trim();
    }

    /**
     * Valida el header x-signature que manda MercadoPago contra el HMAC-SHA256
     * calculado con el webhook secret propio, siguiendo el formato de manifiesto
     * documentado por MercadoPago: "id:{dataId};request-id:{xRequestId};ts:{ts};"
     */
    public boolean esValida(String xSignature, String xRequestId, String dataId) {
        if (xSignature == null || dataId == null) {
            return false;
        }

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

            if (ts == null || hash == null) {
                log.warn("[DEBUG-FIRMA] No se pudo parsear ts/v1 del header x-signature: '{}'", xSignature);
                return false;
            }

            String manifest = construirManifest(dataId, xRequestId, ts);

            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] rawHmac = mac.doFinal(manifest.getBytes(StandardCharsets.UTF_8));

            StringBuilder computed = new StringBuilder();
            for (byte b : rawHmac) computed.append(String.format("%02x", b));

            boolean esValida = computed.toString().equals(hash);

            // TODO: sacar este log una vez resuelto el problema de firma - expone datos de debug
            String secretHash = sha256Hex(webhookSecret);
            log.warn("[DEBUG-FIRMA] manifest='{}' | secretSha256={} | secretLength={} | hashRecibido={} | hashCalculado={} | coincide={}",
                    manifest, secretHash, webhookSecret != null ? webhookSecret.length() : 0, hash, computed, esValida);

            return esValida;

        } catch (Exception e) {
            log.error("Error validando firma de webhook", e);
            return false;
        }
    }

    private String construirManifest(String dataId, String xRequestId, String ts) {
        StringBuilder manifest = new StringBuilder();
        manifest.append("id:").append(dataId.toLowerCase()).append(";");
        if (xRequestId != null) {
            manifest.append("request-id:").append(xRequestId).append(";");
        }
        manifest.append("ts:").append(ts).append(";");
        return manifest.toString();
    }

    // TODO: método de diagnóstico temporal - borrar junto con el log [DEBUG-FIRMA] una vez resuelto
    private String sha256Hex(String value) {
        if (value == null) return "null";
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(value.trim().getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return "error";
        }
    }
}