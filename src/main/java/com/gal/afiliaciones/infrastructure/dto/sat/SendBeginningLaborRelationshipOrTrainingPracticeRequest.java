package com.gal.afiliaciones.infrastructure.dto.sat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SendBeginningLaborRelationshipOrTrainingPracticeRequest {

    private String employerIdType;

    private String employerIdNumber;

    private String decentralizedNitConsecutive;

    private LocalDate laborRelationStartDate;

    private String headquartersCode;

    private String workCenterCode;

    private String workerIdType;

    private String workerIdNumber;

    private String workerFirstName;

    private String workerFirstLastName;

    private int contributorType;

    private int subContributorType;

    private int ibc;

    private int salaryType;

    private LocalDate workerBirthDate;

    private String workerGender;

    private String eps;

    private String afp;

    private String municipality;

    private String address;

    private String locationZone;

    private Long phoneNumber;

    private String email;

    private String position;

    private String workMode;

    private String workDayType;

    private int workAtHeight;

    private String transactionUserIdType;

    private String transactionUserIdNumber;

    private String transactionUserName;

    private LocalDateTime transactionDateTime;

    private String transactionNumber;

}
