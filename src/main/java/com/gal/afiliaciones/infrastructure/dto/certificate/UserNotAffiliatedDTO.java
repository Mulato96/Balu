package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserNotAffiliatedDTO {

    private LocalTime dateConsult;
    private String typeDocument;
    private int numberAttemps;

}
