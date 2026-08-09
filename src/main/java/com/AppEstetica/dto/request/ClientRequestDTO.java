package com.AppEstetica.dto.request;


import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ClientRequestDTO {

    @NotBlank(message = "El nombre es Obligatorio")
    private String name;

    @NotBlank(message = "El telefono es Obligatorio")
    private String phone;


   private String avatarUrl;

   private String email;

}
