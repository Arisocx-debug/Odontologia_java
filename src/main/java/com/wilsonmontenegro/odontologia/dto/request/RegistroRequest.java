package com.wilsonmontenegro.odontologia.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegistroRequest {

    @NotBlank(message = "El nombre es obligatorio")
    @Size(max = 100)
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Size(max = 100)
    private String apellido;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "El correo no es valido")
    private String email;

    @NotBlank(message = "El telefono es obligatorio")
    @Pattern(regexp = "^[0-9]{10}$", message = "El numero de telefono debe tener exactamente 10 digitos")
    private String telefono;

    @NotBlank(message = "La contrasena es obligatoria")
    @Size(min = 6, message = "La contrasena debe tener al menos 6 caracteres")
    private String password;
}
