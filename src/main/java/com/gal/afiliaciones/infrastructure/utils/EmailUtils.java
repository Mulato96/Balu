package com.gal.afiliaciones.infrastructure.utils;

import org.springframework.stereotype.Component;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;

@Component
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmailUtils {

    public static String replaceTemplatePlaceholders(String templateContent, Map<String, Object> datos) {
        String mensaje = templateContent;
        for (Map.Entry<String, Object> entry : datos.entrySet()) {
            String placeholder = "{" + entry.getKey() + "}";
            Object valor = entry.getValue();
            mensaje = mensaje.replace(placeholder, valor.toString());
        }
        return mensaje;
    }

}
