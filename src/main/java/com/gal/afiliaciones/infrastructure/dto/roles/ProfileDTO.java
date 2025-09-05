package com.gal.afiliaciones.infrastructure.dto.roles;

import lombok.Data;

import java.time.LocalDate;

@Data
public class ProfileDTO {

    private Long id;
    private String profileCode;
    private String profileName;
    private String status;
    private LocalDate registrationDate;
    private LocalDate updateDate;
    private String detail;
    private int userCreatorId;
    private Integer userEditorId;
    private String keycloakId;
}
