package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.domain.model.Role;
import com.gal.afiliaciones.infrastructure.dto.login.RoleResponseLoginDTO;

import java.util.List;

public interface RolesUserService {
    List<RoleResponseLoginDTO> getRolesByUser(Long userId);
    List<RoleResponseLoginDTO> getRolesByRoleName(String roleName);
    Boolean updateRoleUser(Long userId,Long roleId);
    Role findByName(String name);
}
