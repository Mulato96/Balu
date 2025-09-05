package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import lombok.Builder;

@Builder
public record AffiliationsFilterDTO(Integer criteria, String fieldValue, String sortBy, String sortOrder) {
}
