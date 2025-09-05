package com.gal.afiliaciones.infrastructure.dto.ruaf;

import lombok.Builder;

import java.util.List;

@Builder
public record RuafFilterDTO(RuafStatus status, String createdAt, List<RuafTypes> reportType, String sortBy, String sortOrder) {
}
