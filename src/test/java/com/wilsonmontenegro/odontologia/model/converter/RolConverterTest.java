package com.wilsonmontenegro.odontologia.model.converter;

import com.wilsonmontenegro.odontologia.model.enums.Rol;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RolConverterTest {
    private final RolConverter converter = new RolConverter();

    @Test
    void convierteRolesLaravelAMayusculasEnLaEntidad() {
        assertEquals(Rol.ADMINISTRADOR, converter.convertToEntityAttribute("administrador"));
        assertEquals("empleado", converter.convertToDatabaseColumn(Rol.EMPLEADO));
    }
}
