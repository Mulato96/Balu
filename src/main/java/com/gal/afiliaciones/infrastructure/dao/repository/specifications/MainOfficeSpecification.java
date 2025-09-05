package com.gal.afiliaciones.infrastructure.dao.repository.specifications;

import com.gal.afiliaciones.domain.model.affiliate.MainOffice;
import com.gal.afiliaciones.infrastructure.dto.address.AddressDTO;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class MainOfficeSpecification {

    private MainOfficeSpecification() {
        throw new IllegalStateException("Utility class");
    }

    public static Specification<MainOffice> findAllByIdUser(Long idUser){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.equal(root.get(Constant.FIELD_OFFICE_MANAGER).get("id"), idUser);

    }

    public static Specification<MainOffice> findByIdUserAndDepartmentAndCity(Long idUser, Long department, Long city) {
        return (root, query, criteriaBuilder) -> {


            Predicate idUserPredicate = criteriaBuilder.equal(root.get(Constant.FIELD_OFFICE_MANAGER).get("id"), idUser);

            Predicate departmentPredicate = department != null
                    ? criteriaBuilder.equal(root.get(Constant.FIELD_ID_DEPARTMENT), department)
                    : null;

            Predicate cityPredicate = city != null
                    ? criteriaBuilder.equal(root.get(Constant.FIELD_ID_CITY), city)
                    : null;

            if (departmentPredicate != null && cityPredicate != null) {
                return criteriaBuilder.and(
                        idUserPredicate,
                        criteriaBuilder.and(departmentPredicate, cityPredicate)
                );

            } else if (departmentPredicate != null) {
                return criteriaBuilder.and(idUserPredicate, departmentPredicate);
            } else if (cityPredicate != null) {
                return criteriaBuilder.and(idUserPredicate, cityPredicate);
            }


            return idUserPredicate;
        };
    }

    public static Specification<MainOffice> findByMainTrue(Long idUser){
        return (root, query, criteriaBuilder) ->
                criteriaBuilder.and(criteriaBuilder.equal(root.get("main"), true), criteriaBuilder.equal(root.get(Constant.FIELD_OFFICE_MANAGER).get("id"), idUser));
    }

    public static Specification<MainOffice> findByIdUserAndDepartmentAndCityAndAddress(Long idUser, AddressDTO addressDTO) {
        return (root, query, criteriaBuilder) -> {

            Predicate idUserPredicate = criteriaBuilder.equal(root.get(Constant.FIELD_OFFICE_MANAGER).get("id"), idUser);

            Predicate departmentPredicate = addressDTO!=null && addressDTO.getIdDepartment() != null
                    ? criteriaBuilder.equal(root.get("idDepartment"), addressDTO.getIdDepartment())
                    : null;

            Predicate cityPredicate = addressDTO!=null && addressDTO.getIdCity() != null
                    ? criteriaBuilder.equal(root.get(Constant.FIELD_ID_CITY), addressDTO.getIdCity())
                    : null;

            Predicate addressPredicate = predicatesAddressDTO(addressDTO, root, criteriaBuilder);

            Predicate combinedPredicate = criteriaBuilder.and(idUserPredicate);
            if (departmentPredicate != null) {
                combinedPredicate = criteriaBuilder.and(combinedPredicate, departmentPredicate);
            }
            if (cityPredicate != null) {
                combinedPredicate = criteriaBuilder.and(combinedPredicate, cityPredicate);
            }

            if (addressPredicate != null) {
                combinedPredicate = criteriaBuilder.and(combinedPredicate, addressPredicate);
            }

            return combinedPredicate;
        };
    }

    public static Specification<MainOffice> findByIdUserAndName(Long idUser, String name) {
        return (root, query, criteriaBuilder) -> criteriaBuilder.and(
                criteriaBuilder.equal(root.get(Constant.FIELD_OFFICE_MANAGER).get("id"), idUser),
                criteriaBuilder.equal(root.get("mainOfficeName"), name));

    }

    private static Predicate predicatesAddressDTO(AddressDTO addressDTO, Root<?> root, CriteriaBuilder criteriaBuilder){

        Predicate addressPredicate = null;

        if (addressDTO != null) {
            List<Predicate> addressPredicates = new ArrayList<>();

            if (addressDTO.getAddress() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("address"), addressDTO.getAddress()));
            }
            if (addressDTO.getIdDepartment() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idDepartment"), addressDTO.getIdDepartment()));
            }
            if (addressDTO.getIdCity() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get(Constant.FIELD_ID_CITY), addressDTO.getIdCity()));
            }
            if (addressDTO.getIdMainStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idMainStreet"), addressDTO.getIdMainStreet()));
            }
            if (addressDTO.getIdNumberMainStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNumberMainStreet"), addressDTO.getIdNumberMainStreet()));
            }
            if (addressDTO.getIdLetter1MainStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idLetter1MainStreet"), addressDTO.getIdLetter1MainStreet()));
            }
            if (addressDTO.getIsBis() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("isBis"), addressDTO.getIsBis()));
            }
            if (addressDTO.getIdLetter2MainStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idLetter2MainStreet"), addressDTO.getIdLetter2MainStreet()));
            }
            if (addressDTO.getIdCardinalPointMainStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idCardinalPointMainStreet"), addressDTO.getIdCardinalPointMainStreet()));
            }
            if (addressDTO.getIdNum1SecondStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNum1SecondStreet"), addressDTO.getIdNum1SecondStreet()));
            }
            if (addressDTO.getIdLetterSecondStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idLetterSecondStreet"), addressDTO.getIdLetterSecondStreet()));
            }
            if (addressDTO.getIdNum2SecondStreet() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNum2SecondStreet"), addressDTO.getIdNum2SecondStreet()));
            }
            if (addressDTO.getIdCardinalPoint2() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idCardinalPoint2"), addressDTO.getIdCardinalPoint2()));
            }
            if (addressDTO.getIdHorizontalProperty1() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idHorizontalProperty1"), addressDTO.getIdHorizontalProperty1()));
            }
            if (addressDTO.getIdNumHorizontalProperty1() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNumHorizontalProperty1"), addressDTO.getIdNumHorizontalProperty1()));
            }
            if (addressDTO.getIdHorizontalProperty2() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idHorizontalProperty2"), addressDTO.getIdHorizontalProperty2()));
            }
            if (addressDTO.getIdNumHorizontalProperty2() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNumHorizontalProperty2"), addressDTO.getIdNumHorizontalProperty2()));
            }
            if (addressDTO.getIdHorizontalProperty3() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idHorizontalProperty3"), addressDTO.getIdHorizontalProperty3()));
            }
            if (addressDTO.getIdNumHorizontalProperty3() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNumHorizontalProperty3"), addressDTO.getIdNumHorizontalProperty3()));
            }
            if (addressDTO.getIdHorizontalProperty4() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idHorizontalProperty4"), addressDTO.getIdHorizontalProperty4()));
            }
            if (addressDTO.getIdNumHorizontalProperty4() != null) {
                addressPredicates.add(criteriaBuilder.equal(root.get("idNumHorizontalProperty4"), addressDTO.getIdNumHorizontalProperty4()));
            }

            if (!addressPredicates.isEmpty()) {
                addressPredicate = criteriaBuilder.and(addressPredicates.toArray(new Predicate[0]));
            }
        }

        return addressPredicate;
    }

}
