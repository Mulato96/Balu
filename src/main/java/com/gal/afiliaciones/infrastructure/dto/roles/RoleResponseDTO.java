package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

@Data
public class RoleResponseDTO {

    private DataDTO data;
    private String message;
    private String errors;
}
