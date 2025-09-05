package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

import java.util.List;

@Data
public class ActiveRolesResponseDTO {

    private List<ActiveRoleDTO> data;
    private String message;
    private String error;
}
