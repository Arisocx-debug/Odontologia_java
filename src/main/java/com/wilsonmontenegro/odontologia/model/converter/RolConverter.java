package com.wilsonmontenegro.odontologia.model.converter;

import com.wilsonmontenegro.odontologia.model.enums.Rol;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.Locale;

/**
 * Conserva la compatibilidad con el esquema Laravel, cuya columna rol usa
 * valores en minusculas (administrador, empleado y cliente).
 */
@Converter
public class RolConverter implements AttributeConverter<Rol, String> {

    @Override
    public String convertToDatabaseColumn(Rol atributo) {
        return atributo == null ? null : atributo.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public Rol convertToEntityAttribute(String valorBaseDeDatos) {
        return valorBaseDeDatos == null ? null : Rol.valueOf(valorBaseDeDatos.toUpperCase(Locale.ROOT));
    }
}
