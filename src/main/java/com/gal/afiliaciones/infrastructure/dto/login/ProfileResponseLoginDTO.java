package com.gal.afiliaciones.infrastructure.dto.login;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProfileResponseLoginDTO {
    private String nameProfile;
    private List<PermissionResponseLoginDTO> permissions;
}
