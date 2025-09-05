package com.gal.afiliaciones.infrastructure.dto.ruaf;

import lombok.Builder;

@Builder
public record RuafDTO(Long id, String fileName, String createdAt, String state, String reportType) {
}
