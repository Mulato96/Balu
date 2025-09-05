package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class DataDTO {

    private Long id;
    private String code;
    private String roleName;
    private StatusDTO status;
    private LocalDateTime createDate;
    private String employeeId;
    private String employeeName;
    private LocalDateTime updateDate;
    private List<ProfileAndPermissionDTO> profileAndPermission;
}
