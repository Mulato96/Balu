package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ActiveRoleDTO {

    private Long id;
    private String code;
    private String roleName;
    private StatusDTO status;
    private LocalDateTime createDate;
    private String employeeId;
    private String employeeName;
    private LocalDateTime updateDate;
}
