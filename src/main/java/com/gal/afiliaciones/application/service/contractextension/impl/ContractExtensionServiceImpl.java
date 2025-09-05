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
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.ICardRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.CertificateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.contractextension.ContractExtensionRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyRepository;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionInfoDTO;
import com.gal.afiliaciones.infrastructure.dto.contractextension.ContractExtensionRequest;
import com.gal.afiliaciones.infrastructure.dto.generalNovelty.SaveGeneralNoveltyRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;

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

        // TODO: Integrar con el SAT
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


}
