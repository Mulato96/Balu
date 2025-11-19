package com.gal.afiliaciones.infrastructure.utils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.Set;

public class CustomPageable {

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "id",
            "code",
            "main",
            "mainOfficeName",
            "address",
            "mainOfficePhoneNumber",
            "mainOfficeDepartment",
            "mainOfficeCity",
            "company",
            "typeAffiliation",
            "phoneOneLegalRepresentative",
            "phoneTwoLegalRepresentative",
            "typeDocumentPersonResponsible",
            "numberDocumentPersonResponsible",
            "legalRepresentativeFullName",
            "businessName",
            "idAffiliate"
    );

    public static Pageable of(int page, int size, String sort, String desc, String defaultSort) {
        String field = validateSortField(sort, defaultSort);
        Sort.Direction direction = (desc != null && desc.equalsIgnoreCase("desc"))
                ? Sort.Direction.DESC
                : Sort.Direction.ASC;

        Sort sortObj = Sort.by(direction, field);
        return PageRequest.of(page, size, sortObj);
    }

    private static String validateSortField(String sort, String defaultSort) {
        if (sort == null || sort.isBlank()) {
            return defaultSort;
        }
        return ALLOWED_SORT_FIELDS.contains(sort) ? sort : defaultSort;
    }
}