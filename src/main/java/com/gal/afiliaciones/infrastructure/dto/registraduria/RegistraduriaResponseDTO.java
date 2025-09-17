package com.gal.afiliaciones.infrastructure.dto.registraduria;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegistraduriaResponseDTO {

    private ConsultationStatusDTO consultationStatus;
    private IdentityCardDataDTO identityCardData;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationStatusDTO {
        private String controlNumber;
        private String errorCode;
        private String errorDescription;
        private String consultationDateTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentityCardDataDTO {
        private String nuip;
        private String errorCode;
        private String firstSurname;
        private String particle;
        private String secondSurname;
        private String firstName;
        private String secondName;
        private String expeditionMunicipality;
        private String expeditionDepartment;
        private String expeditionDate;
        private String identityCardStatus;
        private String resolutionNumber;
        private String resolutionYear;
        private String gender;
        private String birthDate;
        private String informant;
        private String serial;
        private String deathDate;
        private String referenceDate;
        private String affectationDate;
    }

}
