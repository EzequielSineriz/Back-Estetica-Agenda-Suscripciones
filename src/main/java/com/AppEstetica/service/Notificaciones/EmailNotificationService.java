package com.AppEstetica.service.Notificaciones;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;

@Slf4j
@Service
public class EmailNotificationService {

    private final RestClient restClient = RestClient.create("https://api.resend.com");

    @Value("${resend.api-key}") // Cargar desde variable de entorno RESEND_API_KEY
    private String apiKey;

    @Value("${admin.notification-email}") // Cargar desde variable de entorno ADMIN_NOTIFICATION_EMAIL
    private String adminEmail;

    // 3 intentos en total: el original + 2 reintentos, con espera creciente (2s, 4s)
    @Retryable(
            retryFor = RestClientException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
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

        enviarEmail(email, "¡Inscripción confirmada! - " + nombreCurso, htmlContent);

        log.info("Email de confirmación enviado a {}", email);
    }

    // Recover: se ejecuta si los 3 intentos de enviarConfirmacionCurso fallan.
    // La firma tiene que empezar con el mismo tipo de excepción y los mismos parámetros del método original.
    @Recover
    public void recuperarEnvioConfirmacion(RestClientException e, String email, String nombreUsuario, String nombreCurso, Long cursoId) {
        log.error("No se pudo enviar el email de confirmación a {} (curso {}) tras varios intentos: {}",
                email, cursoId, e.getMessage());
        // TODO: acá conviene guardar el intento fallido en una tabla (o mandarte un aviso aparte)
        // para poder reenviarlo manualmente después. Por ahora queda solo en el log.
    }

    @Retryable(
            retryFor = RestClientException.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 2000, multiplier = 2)
    )
    public void enviarAvisoAdmin(String nombreUsuario, String emailUsuario, String nombreCurso, Long cursoId) {
        if (adminEmail == null || adminEmail.isBlank()) {
            log.warn("admin.notification-email no está configurado, no se envía aviso de venta");
            return;
        }

        String htmlContent = """
            <!DOCTYPE html>
            <html>
            <body style="font-family: Arial, sans-serif; background-color: #f4f4f9; padding: 20px;">
                <div style="max-width: 500px; margin: 0 auto; background: #ffffff; padding: 30px; border-radius: 12px;">
                    <h2 style="color: #16a34a; text-align: center;">Nueva venta 💸</h2>
                    <p><strong>%s</strong> (%s) se inscribió y pagó el curso:</p>
                    <p style="font-size: 18px;"><strong>%s</strong></p>
                </div>
            </body>
            </html>
            """.formatted(nombreUsuario, emailUsuario, nombreCurso);

        enviarEmail(adminEmail, "Nueva venta - " + nombreCurso, htmlContent);

        log.info("Email de aviso de venta enviado a la administradora ({})", adminEmail);
    }

    @Recover
    public void recuperarEnvioAdmin(RestClientException e, String nombreUsuario, String emailUsuario, String nombreCurso, Long cursoId) {
        log.error("No se pudo enviar el aviso de venta a la administradora (curso {}, usuario {}) tras varios intentos: {}",
                cursoId, emailUsuario, e.getMessage());
    }

    private void enviarEmail(String destinatario, String asunto, String htmlContent) {
        Map<String, Object> body = Map.of(
                "from", "Health Estética <onboarding@resend.dev>", // Dirección por defecto de prueba en Resend
                "to", new String[]{destinatario},
                "subject", asunto,
                "html", htmlContent
        );

        // OJO: acá NO atajamos la excepción — la dejamos subir para que @Retryable la vea y reintente.
        // Si falla los 3 intentos, cae en el método @Recover correspondiente.
        restClient.post()
                .uri("/emails")
                .header("Authorization", "Bearer " + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }
}