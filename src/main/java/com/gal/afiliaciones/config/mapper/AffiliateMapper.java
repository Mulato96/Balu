package com.gal.afiliaciones.config.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dto.affiliate.EmployerAffiliationHistoryDTO;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface AffiliateMapper {

    EmployerAffiliationHistoryDTO toEmployerAffiliationHistoryDTO(Affiliate affiliate);
    List<EmployerAffiliationHistoryDTO> toEmployerAffiliationHistoryDTOList(List<Affiliate> affiliates);
}
