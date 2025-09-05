package com.gal.afiliaciones.application.service.validatecontributorelationship.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.gal.afiliaciones.config.ex.validationpreregister.AffiliateNotFound;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidRelationShipResponse;
import com.gal.afiliaciones.infrastructure.dto.validatecontributorelationship.ValidateContributorRequest;
import com.gal.afiliaciones.infrastructure.utils.Constant;


class ValidateContributorRelationShipServiceImplTest {

    @Mock
    private AffiliateRepository affiliateRepository;
    @Mock
    private AffiliationDependentRepository affiliationDependentRepository;
    @Mock
    private IAffiliationEmployerDomesticServiceIndependentRepository affiliationRepository;

    @InjectMocks
    private ValidateContributorRelationShipServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    private ValidateContributorRequest buildRequest(String employerType, String employerNumber, String employeeType, String employeeNumber) {
        ValidateContributorRequest req = new ValidateContributorRequest();
        req.setEmployerIdentificationType(employerType);
        req.setEmployerIdentificationNumber(employerNumber);
        req.setEmployeeIdentificationType(employeeType);
        req.setEmployeeIdentificationNumber(employeeNumber);
        return req;
    }

    private Affiliate buildAffiliate(String docType, String docNumber, String nitCompany, String subType, String type, String filedNumber) {
        Affiliate aff = new Affiliate();
        aff.setDocumentType(docType);
        aff.setDocumentNumber(docNumber);
        aff.setNitCompany(nitCompany);
        aff.setAffiliationSubType(subType);
        aff.setAffiliationType(type);
        aff.setFiledNumber(filedNumber);
        return aff;
    }

    @Test
    void validateRelationShip_shouldReturnOk_whenEmployeeFoundAndIsIndependent() {
        ValidateContributorRequest request = buildRequest("CC", "123", "TI", "456");
        Affiliate employer = buildAffiliate("CC", "123", "NIT123", "SUBTYPE", "TYPE", "FILED1");
        Affiliate employee = buildAffiliate("TI", "456", "NIT123", "SUBTYPE", Constant.TYPE_AFFILLATE_INDEPENDENT, "FILED2");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(employer));
        when(affiliateRepository.findByNitCompany("NIT123"))
                .thenReturn(List.of(employer, employee));
        when(affiliationRepository.findByFiledNumber("FILED2"))
                .thenReturn(Optional.of(mockAffiliation()));

        ValidRelationShipResponse response = service.validateRelationShip(request);

        assertEquals("Ok", response.getMessageResponse());
        assertEquals("John", response.getFirstNameContributor());
        assertEquals("Doe", response.getFirstSurNameContributor());
    }

    @Test
    void validateRelationShip_shouldReturnOk_whenEmployeeFoundAndIsDependent() {
        ValidateContributorRequest request = buildRequest("CC", "123", "TI", "456");
        Affiliate employer = buildAffiliate("CC", "123", "NIT123", "SUBTYPE", "TYPE", "FILED1");
        Affiliate employee = buildAffiliate("TI", "456", "NIT123", "SUBTYPE", "OTHER_TYPE", "FILED3");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(employer));
        when(affiliateRepository.findByNitCompany("NIT123"))
                .thenReturn(List.of(employer, employee));
        when(affiliationDependentRepository.findByFiledNumber("FILED3"))
                .thenReturn(Optional.of(mockAffiliationDependent()));

        ValidRelationShipResponse response = service.validateRelationShip(request);

        assertEquals("Ok", response.getMessageResponse());
        assertEquals("Jane", response.getFirstNameContributor());
        assertEquals("Smith", response.getFirstSurNameContributor());
    }

    @Test
    void validateRelationShip_shouldThrowAffiliateNotFound_whenNoAffiliatesFound() {
        ValidateContributorRequest request = buildRequest("CC", "123", "TI", "456");
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(Collections.emptyList());

        assertThrows(AffiliateNotFound.class, () -> service.validateRelationShip(request));
    }

    @Test
    void validateRelationShip_shouldThrowAffiliateNotFound_whenNoAffiliatesByNitCompany() {
        ValidateContributorRequest request = buildRequest("CC", "123", "TI", "456");
        Affiliate employer = buildAffiliate("CC", "123", "NIT123", "SUBTYPE", "TYPE", "FILED1");
        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(employer));
        when(affiliateRepository.findByNitCompany("NIT123"))
                .thenReturn(Collections.emptyList());

        assertThrows(AffiliateNotFound.class, () -> service.validateRelationShip(request));
    }

    @Test
    void validateRelationShip_shouldReturnWarning_whenEmployeeNotFound() {
        ValidateContributorRequest request = buildRequest("CC", "123", "TI", "999");
        Affiliate employer = buildAffiliate("CC", "123", "NIT123", "SUBTYPE", "TYPE", "FILED1");
        Affiliate employee = buildAffiliate("TI", "456", "NIT123", "SUBTYPE", "OTHER_TYPE", "FILED3");

        when(affiliateRepository.findAllByDocumentTypeAndDocumentNumber(anyString(), anyString()))
                .thenReturn(List.of(employer));
        when(affiliateRepository.findByNitCompany("NIT123"))
                .thenReturn(List.of(employer, employee));

        ValidRelationShipResponse response = service.validateRelationShip(request);

        assertEquals("El cotizante no tiene un vínculo laboral vigente con el aportante. ¿Desea continuar de todas formas?", response.getMessageResponse());
    }

    @Test
    void filterNonEmployerAffiliates_shouldFilterCorrectly() {
        Affiliate a1 = buildAffiliate("CC", "1", "NIT", Constant.SUBTYPE_AFFILLATE_EMPLOYER_MERCANTILE, "TYPE", "F1");
        Affiliate a2 = buildAffiliate("CC", "2", "NIT", Constant.TYPE_AFFILLATE_EMPLOYER_DOMESTIC, "TYPE", "F2");
        Affiliate a3 = buildAffiliate("CC", "3", "NIT", "OTHER", "TYPE", "F3");

        List<Affiliate> result = service.filterNonEmployerAffiliates(List.of(a1, a2, a3));
        assertEquals(1, result.size());
        assertEquals("3", result.get(0).getDocumentNumber());
    }

    @Test
    void findEmployee_shouldReturnEmployeeIfExists() {
        Affiliate a1 = buildAffiliate("CC", "1", "NIT", "OTHER", "TYPE", "F1");
        Affiliate a2 = buildAffiliate("TI", "2", "NIT", "OTHER", "TYPE", "F2");
        ValidateContributorRequest req = buildRequest("CC", "NIT", "TI", "2");

        Optional<Affiliate> found = service.findEmployee(List.of(a1, a2), req);
        assertTrue(found.isPresent());
        assertEquals("2", found.get().getDocumentNumber());
    }

    @Test
    void findEmployee_shouldReturnEmptyIfNotExists() {
        Affiliate a1 = buildAffiliate("CC", "1", "NIT", "OTHER", "TYPE", "F1");
        ValidateContributorRequest req = buildRequest("CC", "NIT", "TI", "2");

        Optional<Affiliate> found = service.findEmployee(List.of(a1), req);
        assertFalse(found.isPresent());
    }

    // Helper mocks
    private Affiliation mockAffiliation() {
        Affiliation aff = new Affiliation();
        aff.setFirstName("John");
        aff.setSecondName("Middle");
        aff.setSurname("Doe");
        aff.setSecondSurname("Smith");
        return aff;
    }

    private AffiliationDependent mockAffiliationDependent() {
        AffiliationDependent aff = new AffiliationDependent();
        aff.setFirstName("Jane");
        aff.setSecondName("Middle");
        aff.setSurname("Smith");
        aff.setSecondSurname("Doe");
        return aff;
    }
}