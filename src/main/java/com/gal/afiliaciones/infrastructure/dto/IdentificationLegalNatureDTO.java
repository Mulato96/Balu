package com.gal.afiliaciones.infrastructure.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IdentificationLegalNatureDTO {

    private String idEntity;
    private String nit;
    private String companyName;
    private String department;
    private String municipality;
    private String address;
    private String zipCode;
    private String phone;
    private String fax;
    private String email;
    private String pageWeb;
    private String siif;
}
