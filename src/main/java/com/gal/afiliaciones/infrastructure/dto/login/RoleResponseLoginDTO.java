package com.gal.afiliaciones.infrastructure.dto.login;

import com.gal.afiliaciones.domain.model.WorkspaceRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleResponseLoginDTO {
    private String nameRole;
    private WorkspaceRole workspaceRole;
    private List<ProfileResponseLoginDTO> profiles;
}
