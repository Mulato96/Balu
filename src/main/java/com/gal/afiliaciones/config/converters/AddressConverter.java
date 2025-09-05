package com.gal.afiliaciones.config.converters;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;

import java.util.function.Function;

public class AddressConverter {

    private AddressConverter() {}

    public static final Function<UserMain, AddressDTO> entityToDto = (UserMain entity) -> {
        if (entity == null)
            return null;

        return AddressDTO.builder()
                .address(entity.getAddress())
                .idDepartment(entity.getIdDepartment())
                .idCity(entity.getIdCity())
                .idMainStreet(entity.getIdMainStreet())
                .idNumberMainStreet(entity.getIdNumberMainStreet())
                .idLetter1MainStreet(entity.getIdLetter1MainStreet())
                .isBis(entity.getIsBis())
                .idLetter2MainStreet(entity.getIdLetter2MainStreet())
                .idCardinalPointMainStreet(entity.getIdCardinalPointMainStreet())
                .idNum1SecondStreet(entity.getIdNum1SecondStreet())
                .idLetterSecondStreet(entity.getIdLetterSecondStreet())
                .idNum2SecondStreet(entity.getIdNum2SecondStreet())
                .idCardinalPoint2(entity.getIdCardinalPoint2())
                .idHorizontalProperty1(entity.getIdHorizontalProperty1())
                .idNumHorizontalProperty1(entity.getIdNumHorizontalProperty1())
                .idHorizontalProperty2(entity.getIdHorizontalProperty2())
                .idNumHorizontalProperty2(entity.getIdNumHorizontalProperty2())
                .idHorizontalProperty3(entity.getIdHorizontalProperty3())
                .idNumHorizontalProperty3(entity.getIdNumHorizontalProperty3())
                .idHorizontalProperty4(entity.getIdHorizontalProperty4())
                .idNumHorizontalProperty4(entity.getIdNumHorizontalProperty4())
                .build();
    };

}
