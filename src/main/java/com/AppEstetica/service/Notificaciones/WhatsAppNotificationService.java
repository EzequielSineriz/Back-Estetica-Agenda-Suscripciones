package com.AppEstetica.service.Notificaciones;

import com.AppEstetica.entities.Appointment;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class WhatsAppNotificationService {

    private final RestClient restClient = RestClient.create("https://graph.facebook.com/v20.0");

    @Value("${whatsapp.access-token}")
    private String accessToken;

    @Value("${whatsapp.phone-number-id}")
    private String phoneNumberId;

    private static final DateTimeFormatter FECHA_LEGIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_LEGIBLE = DateTimeFormatter.ofPattern("HH:mm");

    public void enviarRecordatorio(Appointment appointment) {
        String telefono = appointment.getClient().getPhone();
        if (telefono == null || telefono.isBlank()) {
            return;
        }

        // El nombre del template ("recordatorio_turno") tiene que coincidir EXACTO
        // con el que aprobaste en el panel de Meta Business, incluyendo el idioma.
        Map<String, Object> body = Map.of(
                "messaging_product", "whatsapp",
                "to", normalizarTelefono(telefono),
                "type", "template",
                "template", Map.of(
                        "name", "recordatorio_turno",
                        "language", Map.of("code", "es_AR"),
                        "components", List.of(
                                Map.of(
                                        "type", "body",
                                        "parameters", List.of(
                                                Map.of("type", "text", "text", appointment.getClient().getName()),
                                                Map.of("type", "text", "text", appointment.getService()),
                                                Map.of("type", "text", "text", appointment.getDate().format(FECHA_LEGIBLE)),
                                                Map.of("type", "text", "text", appointment.getTime().format(HORA_LEGIBLE))
                                        )
                                )
                        )
                )
        );

        try {
            restClient.post()
                    .uri("/{phoneNumberId}/messages", phoneNumberId)
                    .header("Authorization", "Bearer " + accessToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("WhatsApp de recordatorio enviado a {} para turno {}", telefono, appointment.getId());

        } catch (Exception e) {
            log.error("Error enviando WhatsApp para turno {}: {}", appointment.getId(), e.getMessage());
        }
    }

    // Meta espera el número en formato internacional sin "+" ni espacios, ej: 5491122334455
    private String normalizarTelefono(String telefono) {
        return telefono.replaceAll("[^0-9]", "");
    }
}