package com.gal.afiliaciones.infrastructure.dto.employer;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RequestUpdateLegalRepresentativeDTO {

    private String typeDocumentPersonResponsible;
    private String numberDocumentPersonResponsible;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateBirth;
    private Integer age;
    private String sex;
    private String otherSex;
    private Long nationality;
    private Long eps;
    private Long afp;
    private String phone1;
    private String phone2;
    private String email;

}
