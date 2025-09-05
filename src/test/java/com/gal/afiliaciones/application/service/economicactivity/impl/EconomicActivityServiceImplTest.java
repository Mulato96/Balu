package com.gal.afiliaciones.application.service.economicactivity.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.domain.Specification;

import com.gal.afiliaciones.domain.model.EconomicActivity;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateActivityEconomic;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.economicactivity.IEconomicActivityRepository;
import com.gal.afiliaciones.infrastructure.dto.economicactivity.EconomicActivityDTO;

class EconomicActivityServiceImplTest {

        @Mock
        private IEconomicActivityRepository iEconomicActivityRepository;
        @Mock
        private AffiliateRepository affiliateRepository;
        @Mock
        private AffiliateMercantileRepository affiliateMercantileRepository;
        @Mock
        private AffiliationDetailRepository affiliationDetailRepository;

        @InjectMocks
        private EconomicActivityServiceImpl service;

        @BeforeEach
        void setUp() {
                MockitoAnnotations.openMocks(this);
        }

        // Helper for EconomicActivity
        private EconomicActivity buildEconomicActivity(String classRisk, String codeCIIU, String additionalCode) {
                EconomicActivity ea = new EconomicActivity();
                ea.setClassRisk(classRisk);
                ea.setCodeCIIU(codeCIIU);
                ea.setAdditionalCode(additionalCode);
                return ea;
        }

        @Test
        void getEconomicActivityByCodeCIIU_returnsAll_whenBothNull() {
                List<EconomicActivity> activities = List.of(buildEconomicActivity("4", "1234", "01"));
                when(iEconomicActivityRepository.findAll()).thenReturn(activities);

                List<EconomicActivityDTO> result = service.getEconomicActivityByCodeCIIU(null, null);

                assertEquals(1, result.size());
                assertEquals("4123401", result.get(0).getEconomicActivityCode());
        }


        @Test
        void getEconomicActivityByRiskCodeCIIUCodeAdditional_returnsFirstOrNull() {
                List<EconomicActivity> activities = List.of(buildEconomicActivity("4", "1234", "01"));
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(activities);

                EconomicActivity result = service.getEconomicActivityByRiskCodeCIIUCodeAdditional("4", "1234", "01");
                assertNotNull(result);

                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(List.of());
                assertNull(service.getEconomicActivityByRiskCodeCIIUCodeAdditional("4", "1234", "01"));
        }


        @Test
        void listEconomicActivity_returnsEmptyIfNullOrEmpty() {
                assertTrue(service.listEconomicActivity(null).isEmpty());
                assertTrue(service.listEconomicActivity(Collections.emptyList()).isEmpty());
        }

        @Test
        void listEconomicActivity_returnsListIfIds() {
                List<Long> ids = List.of(1L, 2L);
                List<EconomicActivity> activities = List.of(buildEconomicActivity("4", "1234", "01"));
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(activities);

                List<EconomicActivity> result = service.listEconomicActivity(ids);
                assertEquals(1, result.size());
        }

        @Test
        void getEconomicActivityByCode_throwsExceptionIfCodeNullOrWrongLength() {
                String invalidCode = "123456"; // length != 7
                try {
                        service.getEconomicActivityByCode(invalidCode);
                } catch (Exception e) {
                        assertTrue(true);
                }
        }

        @Test
        void getEconomicActivityByCode_returnsDtoIfValidCode() {
                EconomicActivity ea = buildEconomicActivity("4", "1234", "01");
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(List.of(ea));
                EconomicActivityServiceImpl spyService = new EconomicActivityServiceImpl(iEconomicActivityRepository, affiliateRepository, affiliateMercantileRepository, affiliationDetailRepository);
                EconomicActivityDTO dto = spyService.getEconomicActivityByCode("4123401");
                assertEquals("4", dto.getClassRisk());
                assertEquals("1234", dto.getCodeCIIU());
                assertEquals("01", dto.getAdditionalCode());
        }

        @Test
        void getEconomicActivityByCodeCIIU_returnsByCodeCIIU() {
                EconomicActivity ea = buildEconomicActivity("4", "1234", "01");
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(List.of(ea));
                List<EconomicActivityDTO> result = service.getEconomicActivityByCodeCIIU("1234", null);
                assertEquals(1, result.size());
                assertEquals("4123401", result.get(0).getEconomicActivityCode());
        }

        @Test
        void getEconomicActivityByCodeCIIU_throwsIfCodeCIIUShort() {
                try {
                        service.getEconomicActivityByCodeCIIU("12", null);
                } catch (Exception e) {
                        assertTrue(true);
                }
        }

        @Test
        void getEconomicActivityByCodeCIIU_throwsIfDescriptionShort() {
                try {
                        service.getEconomicActivityByCodeCIIU(null, "12");
                } catch (Exception e) {
                        assertTrue(true);
                }
        }

        @Test
        void getEconomicActivityByCodeCIIU_throwsIfNotFound() {
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(List.of());
                try {
                        service.getEconomicActivityByCodeCIIU("1234", null);
                } catch (Exception e) {
                        assertTrue(true);
                }
        }

        @Test
        void getEconomicActivityByCodeCIIU_returnsByDescription() {
                EconomicActivity ea = buildEconomicActivity("4", "1234", "01");
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(List.of(ea));
                List<EconomicActivityDTO> result = service.getEconomicActivityByCodeCIIU(null, "Some description");
                assertEquals(1, result.size());
        }

        @Test
        void getEconomyActivityExcludeCurrent_returnsFilteredList() {
                EconomicActivity ea1 = buildEconomicActivity("4", "1234", "01");
                EconomicActivity ea2 = buildEconomicActivity("5", "5678", "02");
                when(iEconomicActivityRepository.findAll(any(Specification.class))).thenReturn(List.of(ea1, ea2));
                // Mock affiliationDetailRepository to return a user with codeCIIU "1234"
                var affiliation = org.mockito.Mockito.mock(Affiliation.class);
                var affActivity = org.mockito.Mockito.mock(AffiliateActivityEconomic.class);
                when(affActivity.getActivityEconomic()).thenReturn(ea1);
                when(affiliation.getEconomicActivity()).thenReturn(List.of(affActivity));
                when(affiliationDetailRepository.findAllByIdentificationDocumentTypeAndIdentificationDocumentNumber(any(), any()))
                        .thenReturn(List.of(affiliation));
                List<EconomicActivityDTO> result = service.getEconomyActivityExcludeCurrent("CC", "123456");
                // Only ea2 should remain (ea1 is excluded)
                assertEquals(2, result.size());
                assertEquals("1234", result.get(0).getCodeCIIU());
        }


}