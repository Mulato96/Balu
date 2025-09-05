package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

@Data
public class PermissionDTO {

    private Long permisoId;
    private String permissionName;
    private String code;
    private boolean active;
    private boolean deleted;
    private String keycloakId;

}
