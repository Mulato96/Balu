package com.gal.afiliaciones.config.converters;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dto.user.RequestUserUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.user.UserUpdateDTO;

import java.util.function.Function;
import java.util.stream.Collectors;

public class UserConverter {

    private UserConverter() {
    }

    public static final Function<UserMain, UserUpdateDTO> entityToDto = (UserMain entity) -> {
        if (entity == null)
            return null;

        return UserUpdateDTO.builder()
                .id(entity.getId())
                .identificationType(entity.getIdentificationType())
                .identification(entity.getIdentification())
                .firstName(entity.getFirstName())
                .secondName(entity.getSecondName())
                .surname(entity.getSurname())
                .secondSurname(entity.getSecondSurname())
                .dateBirth(entity.getDateBirth().toString())
                .age(entity.getAge())
                .sex(entity.getSex())
                .nationality(entity.getNationality())
                .address(AddressConverter.entityToDto.apply(entity))
                .phoneNumber(entity.getPhoneNumber())
                .phone2(entity.getPhoneNumber2())
                .email(entity.getEmail())
                .healthPromotingEntity(entity.getHealthPromotingEntity())
                .pensionFundAdministrator(entity.getPensionFundAdministrator())
                .isRegistryData(false)
                .build();
    };

    public static final Function<UserMain, RequestUserUpdateDTO> entityToRequestUpdate = (UserMain entity) -> {
        if (entity == null)
            return null;

        return RequestUserUpdateDTO.builder()
                .idUser(entity.getId().toString())
                .identificationType(entity.getIdentificationType())
                .identification(entity.getIdentification())
                .firstName(entity.getFirstName())
                .secondName(entity.getSecondName() != null ? entity.getSecondName() : null)
                .surName(entity.getSurname())
                .secondSurName(entity.getSecondSurname() != null ?  entity.getSecondSurname() : null)
                .email(entity.getEmail())
                .phoneNumber(entity.getPhoneNumber())
                .phoneNumber2(entity.getPhoneNumber2())
                .position(entity.getPosition())
                .office(entity.getOffice())
                .area(entity.getArea() != null ? entity.getArea().toString() : null)
                .status(entity.getStatus())
                .levelAuthorization(entity.getLevelAuthorization())
                .userName(entity.getUserName())
                .userType(entity.getUserType())
                .roles(entity.getRoles().stream().map(RoleConverter.entityToDto).toList())
                .infoOperator(entity.getInfoOperator())
                .financialOperator(entity.getFinancialOperator())
                .build();
    };

}
