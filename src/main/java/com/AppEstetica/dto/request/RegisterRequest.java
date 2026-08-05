package com.AppEstetica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "The username cannot be blank.")
        @Size(min = 3, max = 30, message = "The username must be between 3 and 30 characters.")
        String username,

        @NotBlank(message = "The email cannot be blank.")
        @Email(message = "The email must be valid.")
        @Size(max = 100, message = "The email must be less than 100 characters.")
        String email,

        @NotBlank(message = "The password cannot be blank.")
        @Size(min = 6, max = 50, message = "The password must be between 6 and 50 characters.")
        String password

){
}
