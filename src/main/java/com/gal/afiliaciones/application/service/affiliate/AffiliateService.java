package com.gal.afiliaciones.application.service.affiliate;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationInProcessRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationInProcessResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.InfoAffiliateDTO;
import org.springframework.web.multipart.MultipartFile;

import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.RequestChannel;
import com.gal.afiliaciones.infrastructure.dto.affiliate.AffiliationResponseDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataStatusAffiliationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.EmployerAffiliationHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationHistoryView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.IndividualWorkerAffiliationView;
import com.gal.afiliaciones.infrastructure.dto.affiliate.RegularizationDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.UserAffiliateDTO;

import jakarta.mail.MessagingException;

public interface AffiliateService {

    List<UserAffiliateDTO> findAffiliationsByTypeAndNumber(String documentType, String documentNumber);
    Affiliate createAffiliate(Affiliate affiliate);
    List<Affiliate> findAll();
    List<DataStatusAffiliationDTO> getDataStatusAffiliations(String numberDocument, String typeDocument);
    void sing(String filedNumber);
    RegularizationDTO regularizationDocuments(String filedNumber, List<MultipartFile> documents);
    Object responseFoundAffiliate(String identificationType, String identificationNumber);
    Boolean getForeignPension(String filedNumber);
    List<Affiliate> findAffiliatesByTypeAndNumber(String documentType, String documentNumber);
    String assignTemporalPass(String idUser);
    List<RequestChannel> findAllRequestChannel();
    AffiliationResponseDTO findUserAffiliate(String documentType, String documentNumber, Integer verificationDigit);
    Long findAffiliate(String documentType, String documentNumber);
    Long getEmployerSize(int numberWorkers);
    BigDecimal calculateIbcAmount(BigDecimal monthlyContractValue, BigDecimal ibcPercentage);
    Boolean affiliateBUs(String idTipoDoc, String idAfiliado) throws MessagingException, IOException, IllegalAccessException;
    List<EmployerAffiliationHistoryDTO> getEmployerAffiliationHistory(String nitCompany, String documentType, String documentNumber);
    List<EmployerAffiliationHistoryDTO> getEmployerAffiliationHistory(String nitCompany, Integer decentralizedNumber);
    IndividualWorkerAffiliationView getIndividualWorkerAffiliation(String nitCompany, String documentType, String documentNumber);
    List<IndividualWorkerAffiliationHistoryView> getIndividualWorkerAffiliationHistory(String documentType, String documentNumber);
    Affiliate getAffiliateCompany(String documentType, String documentNumber);
    AffiliationInProcessResponseDTO findAllAffiliationInProcess(AffiliationInProcessRequestDTO request);
    InfoAffiliateDTO getInfoAffiliate(Long idAffiliate);
}
