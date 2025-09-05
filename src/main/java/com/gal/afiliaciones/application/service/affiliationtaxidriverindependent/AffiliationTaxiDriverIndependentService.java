package com.gal.afiliaciones.application.service.affiliationtaxidriverindependent;

import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliate.WorkCenterAddressIndependentDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationIndependentTaxiDriverStep3DTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentCreateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentPreLoadDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.AffiliationTaxiDriverIndependentUpdateDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationtaxidriverindependent.ContractDTO;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

public interface AffiliationTaxiDriverIndependentService {

    AffiliationTaxiDriverIndependentPreLoadDTO preloadAffiliationData(String identificationType, String identification,
                                                                      String independentType,
                                                                      String identificationTypeIndependent,
                                                                      String identificationIndependent,
                                                                      Long decentralizedConsecutive);

    Long createAffiliation(AffiliationTaxiDriverIndependentCreateDTO affiliationTaxiDriverIndependentCreateDTO);

    void updateAffiliation(AffiliationTaxiDriverIndependentUpdateDTO affiliationTaxiDriverIndependentUpdateDTO);

    Affiliation uploadDocuments(AffiliationIndependentTaxiDriverStep3DTO dto, List<MultipartFile> documents);

    Optional<ContractDTO> findContractForIbcCalculation(String contractType,
                                                  String contractDuration,
                                                  String contractStartDate,
                                                  String contractEndDate,
                                                  String contractMonthlyValue);

    WorkCenterAddressIndependentDTO getWorkCenterAddress(Long contractantId);

    AffiliationTaxiDriverIndependentPreLoadDTO preloadMercantileNotExists(String identificationType,
                                                                          String identification, String error);

    Affiliation createAffiliationStep3FromPila(AffiliationIndependentTaxiDriverStep3DTO dto);

}
