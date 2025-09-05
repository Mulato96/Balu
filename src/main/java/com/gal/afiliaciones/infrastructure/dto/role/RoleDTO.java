package com.gal.afiliaciones.infrastructure.dto.role;

import com.gal.afiliaciones.domain.model.State;
import com.gal.afiliaciones.domain.model.WorkspaceRole;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record RoleDTO(Long id, String code, String roleName, State status, LocalDateTime createdDate, String userId, String userName,
                      LocalDateTime updateDate, WorkspaceRole workspace) {
}
