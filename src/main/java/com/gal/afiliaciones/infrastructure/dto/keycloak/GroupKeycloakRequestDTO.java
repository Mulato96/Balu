package com.gal.afiliaciones.infrastructure.dto.keycloak;


import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GroupKeycloakRequestDTO {


    private String name;
    private String path;
}
