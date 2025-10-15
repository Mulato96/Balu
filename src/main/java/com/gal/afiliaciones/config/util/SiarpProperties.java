package com.gal.afiliaciones.config.util;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class SiarpProperties {

    @Value("${siarp.token-url}")
    private String tokenUrl;

    @Value("${siarp.client-id}")
    private String clientId;

    @Value("${siarp.client-secret}")
    private String clientSecret;

    @Value("${siarp.target-prefix:https://ags-apis-pre-apicast-staging.apps.openshift4.positiva.gov.co}")
    private String targetPrefix;

    @Value("${siarp.consultaAfiliado2.url}")
    private String consultaAfiliado2Url;

    @Value("${siarp.consultaEstadoAfiliado.url}")
    private String consultaEstadoAfiliadoUrl;
}


