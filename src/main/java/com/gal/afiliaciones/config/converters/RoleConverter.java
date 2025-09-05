package com.gal.afiliaciones.config.converters;

import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.infrastructure.dto.role.RoleDTO;

import java.util.function.Function;

public class RoleConverter {

    private RoleConverter() {}

    public static final Function<Role, RoleDTO> entityToDto = (Role entity) -> {
        if (entity == null)
            return null;

        return RoleDTO.builder()
                .id(entity.getId())
                .code(entity.getCode())
                .roleName(entity.getRoleName())
                .status(entity.getStatus())
                .build();
    };

}
