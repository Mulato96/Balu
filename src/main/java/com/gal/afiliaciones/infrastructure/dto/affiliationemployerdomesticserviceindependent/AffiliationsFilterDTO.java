package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record AffiliationsFilterDTO(Integer criteria, String fieldValue, String sortBy, String sortOrder,
                                    LocalDate dateRequest) {
}
