package com.gal.afiliaciones.application.service.contractextension.impl;

import java.time.LocalDate;
import java.util.Optional;
import java.util.stream.Stream;

import org.springframework.stereotype.Service;

import com.gal.afiliaciones.application.service.CertificateService;
import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.contractextension.ContractExtensionService;
import com.gal.afiliaciones.application.service.generalnovelty.impl.GeneralNoveltyServiceImpl;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.domain.model.Card;
import com.gal.afiliaciones.domain.model.ContractExtension;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliate.Certificate;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.contractextension.ContractExtensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.client.generic.independentcontract.UpdateIndependentContractDateClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentcontract.UpdateIndependentContractDateRequest;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionRequest;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;

import java.time.format.DateTimeFormatter;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@AllArgsConstructor
@Slf4j
public class ContractExtensionServiceImpl implements ContractExtensionService {

    public static final String AFFILIATION_NOT_FOUND = "Affiliation not found.";
    private final AffiliateRepository affiliateRepository;
    private final IAffiliationEmployerDomesticServiceIndependentRepository repositoryAffiliation;
    private final PolicyRepository policyRepository;
    private final ICardRepository cardRepository;
    private final CertificateRepository certificateRepository;
    private final GenerateCardAffiliatedService cardAffiliatedService;
    private final CertificateService certificateService;
    private final ContractExtensionRepository contractExtensionRepository;
    private final GeneralNoveltyServiceImpl generalNoveltyServiceImpl;
    private final UpdateIndependentContractDateClient updateIndependentContractDateClient;
    private final AffiliateMercantileRepository affiliateMercantileRepository;

    @Override
    public ContractExtensionInfoDTO getInfoContract(String filedNumber) {

        Optional<Affiliate> affiliate = affiliateRepository.findByFiledNumber(filedNumber);

        if (affiliate.isPresent()) {
            Optional<Affiliation> affiliation = repositoryAffiliation.findByFiledNumber(affiliate.get().getFiledNumber());
            return affiliation.map(this::buildContractExtensionInfoDTO).orElse(null);

        } else {
            throw new AffiliateNotFoundException("El usuario no tiene contratos registrados en nuestra ARL");
        }
    }

    @Override
    public String saveExtensionContract(final ContractExtensionRequest request) {
        Affiliate affiliate = affiliateRepository
                .findByFiledNumber(request.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFoundException(AFFILIATION_NOT_FOUND));

        Affiliation affiliation = repositoryAffiliation
                .findByFiledNumber(affiliate.getFiledNumber())
                .orElseThrow(() -> new AffiliateNotFoundException(AFFILIATION_NOT_FOUND));

        validateAffiliationRisk(affiliation);

        LocalDate currentEndDate = affiliation.getContractEndDate();
        LocalDate newEndDate = request.getContractEndDate();
        validateNewEndDate(currentEndDate, newEndDate);

        updateAffiliationDetails(affiliation, newEndDate, request);
        updateAffiliateDetails(affiliate, newEndDate);
        updatePolicyEffectiveDate(affiliate, newEndDate);

        deleteCardAndCertificate(affiliate);

        cardAffiliatedService.createCardWithoutOtp(affiliate.getFiledNumber());
        certificateService.createCertificate(affiliate, null);

        saveExtension(affiliate, affiliation, newEndDate);

        // Sync contract date update to external Positiva system (non-blocking)
        syncContractDateUpdateToPositiva(affiliate, affiliation, newEndDate, request);

        return "Modificación exitosa, tu modificación de fecha fin de contrato se ha registrado correctamente.";
    }

    private void deleteCardAndCertificate(Affiliate affiliate) {
        Optional<Card> card = cardRepository.findByFiledNumberAndCompany(affiliate.getFiledNumber(), affiliate.getCompany());
        card.ifPresent(cardRepository::delete);

        Optional<Certificate> certificate = certificateRepository.findByFiledNumberAndCompany(affiliate.getFiledNumber(), affiliate.getCompany());
        certificate.ifPresent(certificateRepository::delete);
    }

    private void saveExtension(final Affiliate affiliate, final Affiliation affiliation, final LocalDate newEndDate) {
        contractExtensionRepository.save(ContractExtension.builder()
                .idAfiliation(affiliate.getIdAffiliate())
                .idAfiliationDetal(affiliation.getId())
                .dateTermination(newEndDate)
                .filedNumber(affiliate.getFiledNumber())
                .build());

        SaveGeneralNoveltyRequest request = SaveGeneralNoveltyRequest.builder()
                .idAffiliation(affiliate.getIdAffiliate())
                .filedNumber(affiliate.getFiledNumber())
                .noveltyType(Constant.CONTRACT_EXTENSION_SUCCESSFULL)
                .status(Constant.APPLIED)
                .observation(Constant.CONTRACT_EXTENSION)
                .build();

        generalNoveltyServiceImpl.saveGeneralNovelty(request);
    }

    private void updateAffiliationDetails(final Affiliation affiliation, final LocalDate newEndDate, final ContractExtensionRequest request) {
        affiliation.setContractEndDate(newEndDate);
        affiliation.setContractTotalValue(request.getContractTotalValue());
        affiliation.setContractDuration(request.getContractDuration());
        repositoryAffiliation.save(affiliation);
    }

    private void updateAffiliateDetails(final Affiliate affiliate, final LocalDate newEndDate) {
        affiliate.setRetirementDate(newEndDate);
        affiliateRepository.save(affiliate);
    }

    private void updatePolicyEffectiveDate(final Affiliate affiliate, final LocalDate newEndDate) {
        policyRepository.findByIdAffiliate(affiliate.getIdAffiliate()).forEach(policy -> {
            policy.setEffectiveDateTo(newEndDate);
            policyRepository.save(policy);
        });
    }


    private ContractExtensionInfoDTO buildContractExtensionInfoDTO(Affiliation affiliation) {
        return ContractExtensionInfoDTO.builder()
                .contractStartDate(affiliation.getContractStartDate())
                .contractEndDate(affiliation.getContractEndDate())
                .contractType(affiliation.getContractType())
                .contractQuality(affiliation.getContractQuality())
                .contractTrasnport(affiliation.getTransportSupply())
                .contractDuration(affiliation.getContractDuration())
                .journeyEstablishment(affiliation.getJourneyEstablished())
                .contractMonthlyValue(affiliation.getContractMonthlyValue())
                .contractTotalValue(affiliation.getContractTotalValue())
                .contractIBC(affiliation.getContractIbcValue())
                .build();
    }

    private boolean validateAffiliationRisk(Affiliation affiliation) {
        return Stream.of("1", "2", "3").anyMatch(affiliation.getRisk()::contains);
    }

    private String validateNewEndDate(LocalDate currentEndDate, LocalDate newEndDate) {
        if (newEndDate.isEqual(currentEndDate)) {
            return "La fecha de fin del contrato no puede ser igual a la fecha actual.";
        }

        if (!newEndDate.isAfter(currentEndDate)) {
            return "La nueva fecha de fin del contrato debe ser al menos un día posterior a la fecha actual.";
        }

        if (newEndDate.isAfter(currentEndDate.plusMonths(6))) {
            return "La nueva fecha de fin del contrato no puede ser más de seis meses posterior a la fecha actual.";
        }

        return "";
    }

    /**
     * Sync contract date extension to external Positiva system.
     * Non-blocking operation - logs failures but doesn't throw exceptions.
     * Follows integrations v2 architecture with automatic telemetry.
     * 
     * @param affiliate the Affiliate entity with worker information
     * @param affiliation the Affiliation entity with contract and contractor details
     * @param newEndDate the new contract end date (after extension)
     * @param request the original ContractExtensionRequest with updated values
     */
    private void syncContractDateUpdateToPositiva(
            Affiliate affiliate, 
            Affiliation affiliation, 
            LocalDate newEndDate,
            ContractExtensionRequest request) {
        try {
            // Null safety checks
            if (affiliate == null || affiliation == null) {
                log.debug("Skipping contract date sync - affiliate or affiliation is null");
                return;
            }

            // Verify this is an independent contractor affiliation
            if (affiliation.getIdentificationDocumentTypeContractor() == null || 
                affiliation.getIdentificationDocumentNumberContractor() == null) {
                log.debug("Skipping contract date sync - no contractor information. FiledNumber={}", 
                        affiliate.getFiledNumber());
                return;
            }

            log.info("Attempting to sync contract date extension to Positiva. Worker={}-{}, Contractor={}-{}, NewDate={}", 
                    affiliate.getDocumentType(), 
                    affiliate.getDocumentNumber(),
                    affiliation.getIdentificationDocumentTypeContractor(),
                    affiliation.getIdentificationDocumentNumberContractor(),
                    newEndDate);
            
            // Build request for external system
            UpdateIndependentContractDateRequest updateRequest = buildUpdateIndependentContractDateRequest(
                    affiliate, affiliation, newEndDate, request);
            
            // Call external system
            Object response = updateIndependentContractDateClient.update(updateRequest);
            
            log.info("Successfully synced contract date extension to Positiva. Worker={}-{}, FiledNumber={}, Response={}", 
                    affiliate.getDocumentType(), 
                    affiliate.getDocumentNumber(),
                    affiliate.getFiledNumber(),
                    response);
                    
        } catch (Exception ex) {
            // Non-blocking: log warning but don't throw - local extension already succeeded
            log.warn("Failed to sync contract date extension to Positiva (non-blocking). Worker={}-{}, FiledNumber={}, Error={}", 
                    safeGet(() -> affiliate.getDocumentType(), "?"),
                    safeGet(() -> affiliate.getDocumentNumber(), "?"),
                    safeGet(() -> affiliate.getFiledNumber(), "?"),
                    ex.getMessage(), ex);
        }
    }

    /**
     * Build UpdateIndependentContractDateRequest from contract extension data.
     * Maps local entities to external API request format following the same pattern as insertion.
     * 
     * @param affiliate worker information
     * @param affiliation contract and contractor information
     * @param newEndDate new end date after extension
     * @param request original extension request with updated values
     * @return populated UpdateIndependentContractDateRequest
     */
    private UpdateIndependentContractDateRequest buildUpdateIndependentContractDateRequest(
            Affiliate affiliate,
            Affiliation affiliation,
            LocalDate newEndDate,
            ContractExtensionRequest request) {
        
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Get subempresa from contractor (same as insertion pattern)
        Integer subempresa = findIdSubEmployer(
                affiliation.getIdentificationDocumentTypeContractor(), 
                affiliation.getIdentificationDocumentNumberContractor());
        
        return UpdateIndependentContractDateRequest.builder()
                // Contractor/employer identification
                .idTipoDocEmp(safeGet(() -> affiliation.getIdentificationDocumentTypeContractor(), ""))
                .idEmpresa(safeGet(() -> affiliation.getIdentificationDocumentNumberContractor(), ""))
                .subempresa(subempresa) // decentralizedConsecutive from AffiliateMercantile
                
                // Worker identification
                .idTipoDocPers(safeGet(() -> affiliate.getDocumentType(), ""))
                .idPersona(safeGet(() -> affiliate.getDocumentNumber(), ""))
                
                // Contract dates
                .fechaInicio(safeGet(() -> affiliation.getContractStartDate().format(dateFormatter), ""))
                .fechaFin(safeGet(() -> newEndDate.format(dateFormatter), ""))
                
                // Extension flag - "S" for Si (Yes), indicating this is an extension/prorroga
                .prorroga("S")
                
                // Contract value - use updated value from request, or existing value
                .valorContrato(safeGet(() -> 
                        request.getContractTotalValue() != null ? 
                        request.getContractTotalValue().intValue() : 
                        affiliation.getContractTotalValue().intValue(), 0))
                
                .build();
    }

    /**
     * Find subempresa ID from contractor's AffiliateMercantile record.
     * Follows same pattern as AffiliateServiceImpl.findIdSubEmployer().
     * 
     * @param documentType contractor document type
     * @param documentNumber contractor document number
     * @return decentralizedConsecutive or 0 if not found
     */
    private Integer findIdSubEmployer(String documentType, String documentNumber) {
        try {
            if (documentType == null || documentNumber == null) {
                return 0;
            }
            
            // Find contractor by document, defaulting to decentralizedConsecutive = 0
            // This matches the insertion pattern from AffiliateServiceImpl
            Optional<AffiliateMercantile> contractorOpt = affiliateMercantileRepository
                    .findFirstByTypeDocumentIdentificationAndNumberIdentificationOrderByFiledNumberDesc(
                            documentType, documentNumber);
            
            if (contractorOpt.isPresent()) {
                AffiliateMercantile contractor = contractorOpt.get();
                return contractor.getDecentralizedConsecutive() != null ? 
                        contractor.getDecentralizedConsecutive().intValue() : 0;
            }
            
            return 0;
        } catch (Exception e) {
            log.warn("Error finding subemployer for contractor {}-{}: {}", 
                    documentType, documentNumber, e.getMessage());
            return 0;
        }
    }

    /**
     * Safely get a value from a supplier, returning a default if exception occurs.
     * Helper method for non-blocking integrations.
     */
    private <T> T safeGet(java.util.function.Supplier<T> supplier, T defaultValue) {
        try {
            T value = supplier.get();
            return value != null ? value : defaultValue;
        } catch (Exception e) {
            return defaultValue;
        }
    }

}
