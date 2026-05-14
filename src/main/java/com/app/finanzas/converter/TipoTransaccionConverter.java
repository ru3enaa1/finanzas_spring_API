package com.app.finanzas.converter;

import com.app.finanzas.entity.TipoTransaccion;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TipoTransaccionConverter implements AttributeConverter<TipoTransaccion, String> {

    @Override
    public String convertToDatabaseColumn(TipoTransaccion attribute) {
        return attribute != null ? attribute.getLabel() : null;
    }

    @Override
    public TipoTransaccion convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isBlank()) {
            return null;
        }
        return TipoTransaccion.fromValue(dbData);
    }
}
