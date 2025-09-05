package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

import java.util.List;

@Data
public class ProfileAndPermissionDTO {

    private List<PermissionDTO> permissions;
    private ProfileDTO idProfile;
}
