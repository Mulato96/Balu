package com.gal.afiliaciones.config.converters;

import com.gal.afiliaciones.domain.model.AffiliationsView;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.ManagementAffiliationDTO;

import java.util.function.Function;

public class AffiliationAdapter {

    private AffiliationAdapter() {}

    public static Function<AffiliationsView, ManagementAffiliationDTO> entityToDto = (AffiliationsView entity) -> {
        if (entity == null)
            return null;

        return ManagementAffiliationDTO.builder()
                .id(entity.getId())
                .typeAffiliation(entity.getAffiliationType())
                .field(entity.getFiledNumber())
                .stageManagement(entity.getStageManagement())
                .dateRequest(entity.getDateRequest())
                .nameOrSocialReason(entity.getNameOrSocialReason())
                .numberDocument(entity.getNumberDocument())
                .cancelled(entity.getCancelled())
                .dateInterview(entity.getDateInterview() != null ? entity.getDateInterview().toString() : "")
                .dateRegularization(entity.getDateRegularization() != null ? entity.getDateRegularization().toString() : "sin informacion")
                .asignadoA(entity.getAsignadoA() != null ? entity.getAsignadoA() : "sin informacion")
                .idAffiliate(entity.getIdAffiliate())
                .build();
    };

}
