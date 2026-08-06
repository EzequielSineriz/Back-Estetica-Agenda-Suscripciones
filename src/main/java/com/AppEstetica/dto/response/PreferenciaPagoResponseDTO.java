package com.AppEstetica.dto.response;

public record PreferenciaPagoResponseDTO(
        String initPoint,  //URL a la que se redirige al usuario al momento de pagar
        Long inscripcionId
) {
}
