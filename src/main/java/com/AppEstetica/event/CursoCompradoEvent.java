package com.AppEstetica.event;

/**
 * Se publica desde PagoService cuando MercadoPago confirma un pago de curso.
 * Cualquier listener (hoy: notificaciones por mail) reacciona a esto de forma
 * desacoplada y asíncrona, sin bloquear la respuesta al webhook.
 */
public record CursoCompradoEvent(
        String emailUsuario,
        String nombreUsuario,
        String nombreCurso,
        Long cursoId,
        Long inscripcionId
) {
}
