package com.gal.afiliaciones.application.service.ibc.impl;

import com.gal.afiliaciones.application.service.ibc.IBCDetailService;
import com.gal.afiliaciones.domain.model.ibc.IBCDetail;
import com.gal.afiliaciones.infrastructure.dao.repository.ibc.IBCDetailDAO;
import com.gal.afiliaciones.infrastructure.dto.ibc.IBCDetailDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class IBCDetailServiceImpl implements IBCDetailService {

    private static final BigDecimal MINIMUM_WAGE_2024 = new BigDecimal("1300000");
    private static final BigDecimal MAXIMUM_WAGE = MINIMUM_WAGE_2024.multiply(new BigDecimal("25"));

    private final IBCDetailDAO ibcDetailDAO;

    @Override
    public IBCDetailDTO calculateAndSaveIBC(IBCDetailDTO ibcDetailDTO) {
        BigDecimal ibcValue = ibcDetailDTO.getMonthlyContractValue().multiply(new BigDecimal("0.40"));

        switch (ibcDetailDTO.getContractType()) {
            case "Taxista":
                ibcDetailDTO.setIbcValue(ibcValue);
                break;

            case "Contrato prestación de servicios":
            case "Consejal/Edil":
                if (ibcValue.compareTo(MINIMUM_WAGE_2024) < 0) {
                    ibcDetailDTO.setIbcValue(MINIMUM_WAGE_2024);
                } else if (ibcValue.compareTo(MAXIMUM_WAGE) > 0) {
                    ibcDetailDTO.setIbcValue(MAXIMUM_WAGE);
                } else {
                    ibcDetailDTO.setIbcValue(ibcValue);
                }
                break;

            case "Voluntario":
                ibcDetailDTO.setIbcValue(null);  // El valor inicial está vacío
                break;

            default:
                throw new IllegalArgumentException("Tipo de contrato no soportado: " + ibcDetailDTO.getContractType());
        }

        IBCDetail ibcDetail = convertToEntity(ibcDetailDTO);
        ibcDetail = ibcDetailDAO.createOrUpdateIBCDetail(ibcDetail);
        return convertToDTO(ibcDetail);
    }

    private IBCDetail convertToEntity(IBCDetailDTO dto) {
        return IBCDetail.builder()
                .contractType(dto.getContractType())
                .startDate(dto.getStartDate())
                .endDate(dto.getEndDate())
                .contractDuration(dto.getContractDuration())
                .totalContractValue(dto.getTotalContractValue())
                .monthlyContractValue(dto.getMonthlyContractValue())
                .ibcValue(dto.getIbcValue())
                .build();
    }

    private IBCDetailDTO convertToDTO(IBCDetail entity) {
        return IBCDetailDTO.builder()
                .id(entity.getId())
                .contractType(entity.getContractType())
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .contractDuration(entity.getContractDuration())
                .totalContractValue(entity.getTotalContractValue())
                .monthlyContractValue(entity.getMonthlyContractValue())
                .ibcValue(entity.getIbcValue())
                .build();
    }


}
