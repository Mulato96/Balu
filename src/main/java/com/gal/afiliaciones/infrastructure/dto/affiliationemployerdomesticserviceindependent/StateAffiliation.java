package com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StateAffiliation {

    private String fieldNumber;
    private Boolean rejectAffiliation;
    private String reasonReject;
    private Long idOfficial;
    private List<String> comment;
}
