package com.gal.afiliaciones.infrastructure.dto.consultationform;


import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EmployerUpdateDTO {

    private String filedNumber;
    private String registrationDate;
    private String economicActivity;
    private String updateType;

    private String observation;

}
