package com.gal.afiliaciones.infrastructure.dto.usernotification;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserNotificationDTO {

    private String identificationType;
    private String identificationNumber;
    private String completeName;
    private Long idUser;
    private String affiliationType;
    private String affiliationSubtype;
    private String address;
    private String phone;
    private String email;

}
