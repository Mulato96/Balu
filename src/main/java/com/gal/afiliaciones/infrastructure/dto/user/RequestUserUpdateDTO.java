package com.gal.afiliaciones.infrastructure.dto.user;

import com.gal.afiliaciones.domain.model.Operator;
import com.gal.afiliaciones.infrastructure.dto.role.RoleDTO;
import lombok.Builder;

import java.util.List;

@Builder
public record RequestUserUpdateDTO(String idUser, String identificationType, String identification, String firstName, String secondName,
                                   String surName, String secondSurName, String email, String phoneNumber, String phoneNumber2, Integer position,
                                   Integer office, String area, Long status, String levelAuthorization, String userName, Long userType, List<RoleDTO> roles, Operator infoOperator, Operator financialOperator) {
}
