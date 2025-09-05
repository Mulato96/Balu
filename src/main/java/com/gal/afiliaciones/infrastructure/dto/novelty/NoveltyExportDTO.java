package com.gal.afiliaciones.infrastructure.dto.novelty;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

@Builder
public record NoveltyExportDTO(@JsonProperty("Canal de radicación") String channel, @JsonProperty("Fecha de recibido") String date, @JsonProperty("Identificación - Aportante") String documentNumber,
    @JsonProperty("Nombre/Razón social - Aportante") String nameContributor, @JsonProperty("Identificación - cotizante") String contributantIdentification,
    @JsonProperty("Nombres - cotizante") String contributantName, @JsonProperty("Tipo de novedad") String noveltyType, @JsonProperty("Estado") String state,
    @JsonProperty("Causal") String causal) {
}
