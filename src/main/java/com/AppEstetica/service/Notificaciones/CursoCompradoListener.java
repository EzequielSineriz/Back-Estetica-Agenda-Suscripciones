package com.AppEstetica.service.Notificaciones;

import com.AppEstetica.event.CursoCompradoEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Escucha CursoCompradoEvent (publicado por PagoService cuando MercadoPago aprueba el pago)
 * y dispara las dos notificaciones por mail. Corre en un hilo aparte gracias a @Async,
 * así el webhook de MercadoPago no espera a que Resend responda.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CursoCompradoListener {

    private final EmailNotificationService emailNotificationService;

    @Async
    @EventListener
    public void onCursoComprado(CursoCompradoEvent event) {
        log.info("Procesando notificaciones para inscripción {} (curso {})",
                event.inscripcionId(), event.cursoId());

        emailNotificationService.enviarConfirmacionCurso(
                event.emailUsuario(),
                event.nombreUsuario(),
                event.nombreCurso(),
                event.cursoId()
        );

        emailNotificationService.enviarAvisoAdmin(
                event.nombreUsuario(),
                event.emailUsuario(),
                event.nombreCurso(),
                event.cursoId()
        );
    }
}