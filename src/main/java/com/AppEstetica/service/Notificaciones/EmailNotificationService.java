package com.AppEstetica.service.Notificaciones;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Slf4j
@Service
public class EmailNotificationService {

    private final RestClient restClient = RestClient.create("https://api.resend.com");

    @Value("${resend.api-key}") // Cargar desde variable de entorno RESEND_API_KEY
    private String apiKey;

    public void enviarConfirmacionCurso(String email, String nombreUsuario, String nombreCurso, Long cursoId) {
        if (email == null || email.isBlank()) return;

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 20px;">
                <div style="max-width: 500px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 12px;">
                    <h2 style="color: #d946ef; text-align: center;">¡Pago Confirmado! 🎉</h2>
                    <p>Hola <strong>%s</strong>,</p>
                    <p>Tu inscripción al curso <strong>%s</strong> se procesó exitosamente.</p>
                    <p>Ya podés ingresar a la plataforma para ver los módulos:</p>
                    <a href="https://www.healthestetica.com/academy/curso/%d" 
                       style="display: block; width: 200px; margin: 20px auto; padding: 12px; background: #d946ef; color: #ffffff; text-align: center; text-decoration: none; font-weight: bold; border-radius: 8px;">
                       Acceder al Curso
                    </a>
                </div>
            </body>
            </html>
            """.formatted(nombreUsuario, nombreCurso, cursoId);

        Map<String, Object> body = Map.of(
                "from", "Health Estética <onboarding@resend.dev>", // Dirección por defecto de prueba en Resend
                "to", new String[]{email},
                "subject", "¡Inscripción confirmada! - " + nombreCurso,
                "html", htmlContent
        );

        try {
            restClient.post()
                    .uri("/emails")
                    .header("Authorization", "Bearer " + apiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Email enviado exitosamente vía Resend API a {}", email);

        } catch (Exception e) {
            log.error("Error enviando email vía Resend API a {}: {}", email, e.getMessage());
        }
    }
}