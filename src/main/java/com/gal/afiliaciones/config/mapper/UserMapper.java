package com.gal.afiliaciones.config.mapper;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.infrastructure.dto.user.UserUpdateDTO;
import org.mapstruct.*;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface UserMapper {

    @Mapping(source = "phone2", target = "phoneNumber2")
    @Mapping(target = "dateBirth", dateFormat = "yyyy-MM-dd")
    @Mapping(target = "idDepartment", source = "address.idDepartment")
    @Mapping(target = "idCity", source = "address.idCity")
    @Mapping(target = "idMainStreet", source = "address.idMainStreet")
    @Mapping(target = "idNumberMainStreet", source = "address.idNumberMainStreet")
    @Mapping(target = "idLetter1MainStreet", source = "address.idLetter1MainStreet")
    @Mapping(target = "isBis", source = "address.isBis")
    @Mapping(target = "idLetter2MainStreet", source = "address.idLetter2MainStreet")
    @Mapping(target = "idCardinalPointMainStreet", source = "address.idCardinalPointMainStreet")
    @Mapping(target = "idNum1SecondStreet", source = "address.idNum1SecondStreet")
    @Mapping(target = "idLetterSecondStreet", source = "address.idLetterSecondStreet")
    @Mapping(target = "idNum2SecondStreet", source = "address.idNum2SecondStreet")
    @Mapping(target = "idCardinalPoint2", source = "address.idCardinalPoint2")
    @Mapping(target = "idHorizontalProperty1", source = "address.idHorizontalProperty1")
    @Mapping(target = "idNumHorizontalProperty1", source = "address.idNumHorizontalProperty1")
    @Mapping(target = "idHorizontalProperty2", source = "address.idHorizontalProperty2")
    @Mapping(target = "idNumHorizontalProperty2", source = "address.idNumHorizontalProperty2")
    @Mapping(target = "idHorizontalProperty3", source = "address.idHorizontalProperty3")
    @Mapping(target = "idNumHorizontalProperty3", source = "address.idNumHorizontalProperty3")
    @Mapping(target = "idHorizontalProperty4", source = "address.idHorizontalProperty4")
    @Mapping(target = "idNumHorizontalProperty4", source = "address.idNumHorizontalProperty4")
    @Mapping(target = "address", source = "address.address")
    void requestUpdateToUser(UserUpdateDTO dto, @MappingTarget UserMain user);

}
