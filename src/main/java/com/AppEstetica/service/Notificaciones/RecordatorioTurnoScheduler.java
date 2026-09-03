package com.AppEstetica.service.Notificaciones;

import com.AppEstetica.entities.Appointment;
import com.AppEstetica.repository.AppointmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecordatorioTurnoScheduler {

    private final AppointmentRepository appointmentRepository;
    private final EmailNotificationService emailService;
    private final WhatsAppNotificationService whatsAppService;

    // Corre todos los días a las 9:00 AM (hora del servidor -- confirmar que Render esté en UTC
    // y ajustar el cron si hace falta, ver nota más abajo)
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void enviarRecordatoriosDelDia() {
        LocalDate manana = LocalDate.now().plusDays(1);
        List<Appointment> turnos = appointmentRepository.findTurnosParaRecordar(manana);

        log.info("Enviando recordatorios para {} turnos del {}", turnos.size(), manana);

        for (Appointment turno : turnos) {
            try {
                emailService.enviarRecordatorioTurno(turno);
                whatsAppService.enviarRecordatorio(turno);

                turno.setRecordatorioEnviado(true);
                appointmentRepository.save(turno);

            } catch (Exception e) {
                // Un turno que falla no debe frenar el resto de la tanda
                log.error("Error procesando recordatorio del turno {}: {}", turno.getId(), e.getMessage());
            }
        }
    }
}