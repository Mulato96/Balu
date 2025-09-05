package com.gal.afiliaciones.infrastructure.dto.affiliationemployerprovisionserviceindependent;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InformationIndependentWorkerDTO {
    private String firstNameIndependentWorker;
    private String secondNameIndependentWorker;
    private String surnameIndependentWorker;
    private String secondSurnameIndependentWorker;
    private LocalDate dateOfBirthIndependentWorker;
    private String age;
    private Long nationalityIndependentWorker;
    private String gender;
    private String otherGender;
    private Long pensionFundAdministrator;
    private Boolean isForeignPension;
    private Long healthPromotingEntity;
    private AddressIndependentWorkerDTO addressIndependentWorkerDTO;
    private String phone1IndependentWorker;
    private String phone2IndependentWorker;
    private String emailIndependentWorker;
    private String occupation;
}
