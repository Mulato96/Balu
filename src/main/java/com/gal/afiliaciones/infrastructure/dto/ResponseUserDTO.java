package com.gal.afiliaciones.infrastructure.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gal.afiliaciones.domain.model.Operator;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.dto.otp.OTPDataResponseDTO;
import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResponseUserDTO {
    private Long id;
    private String identificationType;
    private String identification;
    private String firstName;
    private String secondName;
    private String surname;
    private String secondSurname;
    private LocalDate dateBirth;
    private int age;
    private String sex;
    private String otherSex;
    private Long nationality;
    private AddressDTO address;
    private String phoneNumber;
    private String phone2;
    private String email;
    private Boolean statusPreRegister;
    private Boolean statusActive;
    private Boolean statusStartAfiiliate;
    private Boolean statusInactive;
    private LocalDateTime lastAffiliationAttempt;
    private LocalDateTime statusInactiveSince;
    private Integer loginAttempts;
    private LocalDateTime lockoutTime;
    private Integer validAttempts;
    private LocalDateTime validOutTime;
    private Integer generateAttempts;
    private LocalDateTime generateOutTime;
    private String profileImage;
    private String profile;
    private LocalDateTime lastPasswordUpdate;
    private Boolean isPasswordExpired;
    private Long healthPromotingEntity;
    private Long pensionFundAdministrator;
    private OTPDataResponseDTO otpData;
    private LocalDateTime lastUpdateDate;
    private Long userType;
    private Operator InfoOperator;
    private Operator financialOperator;
    private Boolean isInArrearsStatus;
    @Enumerated(EnumType.STRING)
    private TypeUser typeUser;
    private String userName;
    private LocalDateTime employerUpdateTime;
}
