package com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployerUpdateDTO {
    private String docType;
    private String docNumber;
    private String subCompanyCode;
    private String email;
    private String addressFull;
    private String phone;
    private String departmentId;
    private String cityId;
    private String legalRepDocType;
    private String legalRepDocNumber;
    private String legalRepFirstName;
    private String legalRepSecondName;
    private String legalRepLastName;
    private String legalRepSecondLastName;
    private String businessName;
}
