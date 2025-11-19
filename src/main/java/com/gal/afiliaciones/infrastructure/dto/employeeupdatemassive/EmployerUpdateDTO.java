package com.gal.afiliaciones.infrastructure.dto.employeeupdatemassive;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmployerUpdateDTO {
    private String docType;
    private String docNumber;
    private String businessName;
    private String departmentId;
    private String cityId;
    private String addressFull;
    private String phone1;
    private String phone2;
    private String email;
}
