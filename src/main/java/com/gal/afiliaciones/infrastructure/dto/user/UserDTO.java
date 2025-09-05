package com.gal.afiliaciones.infrastructure.dto.user;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private Long id;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private String companyName;
    private Long userType;
    private Long status;
    private String email;
    private String phoneNumber;
    private String password;
    private String pin;
    private Timestamp createDate;
    private String identificationType;
    private String identification;
    private Integer verificationDigit;

}
