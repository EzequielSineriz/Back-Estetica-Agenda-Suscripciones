package com.AppEstetica.service.Notificaciones;

import com.AppEstetica.entities.Appointment;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    private final JavaMailSender mailSender;
    private final IcsGenerator icsGenerator;

    private static final DateTimeFormatter FECHA_LEGIBLE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter HORA_LEGIBLE = DateTimeFormatter.ofPattern("HH:mm");

    public void enviarRecordatorio(Appointment appointment) {
        String email = appointment.getClient().getEmail();
        if (email == null || email.isBlank()) {
            return; // sin email, no hay nada que mandar -- no es un error
        }

        try {
            MimeMessage mensaje = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mensaje, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Recordatorio de tu turno mañana - Health Estética");
            helper.setText("""
                    Hola %s,

                    Te recordamos tu turno para mañana:

                    Servicio: %s
                    Fecha: %s
                    Hora: %s

                    Adjuntamos un archivo para que puedas agendarlo directo en tu Google Calendar
                    (abrí el archivo adjunto y elegí "Agregar a mi calendario").

                    ¡Te esperamos!
                    Health Estética
                    """.formatted(
                    appointment.getClient().getName(),
                    appointment.getService(),
                    appointment.getDate().format(FECHA_LEGIBLE),
                    appointment.getTime().format(HORA_LEGIBLE)
            ));

            helper.addAttachment("turno.ics", () -> new java.io.ByteArrayInputStream(icsGenerator.generar(appointment)));

            mailSender.send(mensaje);
            log.info("Email de recordatorio enviado a {} para turno {}", email, appointment.getId());

        } catch (Exception e) {
            log.error("Error enviando email de recordatorio para turno {}: {}", appointment.getId(), e.getMessage());
        }
    }
}