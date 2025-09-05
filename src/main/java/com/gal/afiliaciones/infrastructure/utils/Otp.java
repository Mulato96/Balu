package com.gal.afiliaciones.infrastructure.utils;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class Otp {

    private Otp() {}
    public static Map<String, Object> convertirDTOaMap(Object dto) throws IllegalAccessException {
        Map<String, Object> mapa = new HashMap<>();
        Field[] campos = dto.getClass().getDeclaredFields();
        for (Field campo : campos) {
            if (campo.trySetAccessible()) {
                Object valor = campo.get(dto);
                if (valor != null) {
                    mapa.put(campo.getName(), valor);
                }
            }
        }
        return mapa;
    }

}
