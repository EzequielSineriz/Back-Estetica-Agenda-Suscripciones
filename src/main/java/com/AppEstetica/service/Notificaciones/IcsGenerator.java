package com.AppEstetica.service.Notificaciones;

import com.AppEstetica.entities.Appointment;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class IcsGenerator {

    private static final DateTimeFormatter ICS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss");

    public byte[] generar(Appointment appointment) {
        LocalDateTime inicio = LocalDateTime.of(appointment.getDate(), appointment.getTime());
        LocalDateTime fin = LocalDateTime.of(appointment.getDate(), appointment.getEndTime());
        LocalDateTime ahora = LocalDateTime.now();

        String ics = """
                BEGIN:VCALENDAR
                VERSION:2.0
                PRODID:-//AppEstetica//Turnos//ES
                BEGIN:VEVENT
                UID:turno-%d@healthestetica.com
                DTSTAMP:%s
                DTSTART:%s
                DTEND:%s
                SUMMARY:Turno - %s
                DESCRIPTION:Tu turno en Health Est\u00e9tica para %s
                LOCATION:Health Est\u00e9tica
                END:VEVENT
                END:VCALENDAR
                """.formatted(
                appointment.getId(),
                ahora.truncatedTo(ChronoUnit.SECONDS).format(ICS_FORMAT),
                inicio.format(ICS_FORMAT),
                fin.format(ICS_FORMAT),
                appointment.getService(),
                appointment.getService()
        );

        return ics.getBytes();
    }
}