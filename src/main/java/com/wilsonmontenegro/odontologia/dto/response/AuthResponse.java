package com.wilsonmontenegro.odontologia.dto.response;

import com.wilsonmontenegro.odontologia.model.enums.Rol;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {
    private String token;
    private Long id;
    private String nombre;
    private String email;
    private Rol rol;
    private String redirectUrl;
}
