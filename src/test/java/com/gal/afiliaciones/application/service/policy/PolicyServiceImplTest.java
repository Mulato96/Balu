package com.gal.afiliaciones.application.service.policy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.application.service.policy.impl.PolicyServiceImpl;
import com.gal.afiliaciones.domain.model.Policy;
import com.gal.afiliaciones.infrastructure.dao.repository.policy.PolicyDao;


class PolicyServiceImplTest {

    private PolicyDao policyDao;
    private PolicyServiceImpl policyService;

    @BeforeEach
    void setUp() {
        policyDao = mock(PolicyDao.class);
        policyService = new PolicyServiceImpl(policyDao);
    }

    @Test
    void testCreatePolicy() {
        String idType = "CC";
        String idNumber = "12345";
        LocalDate effectiveDateFrom = LocalDate.now();
        Long idPolicyType = 1L;
        Long idAffiliate = 2L;
        Long codeDesentralice = 1L;
        String nameCompany = "name company";
        Policy expectedPolicy = mock(Policy.class);

        when(policyDao.createPolicy(idType, idNumber, effectiveDateFrom, idPolicyType, idAffiliate, codeDesentralice, nameCompany))
                .thenReturn(expectedPolicy);

        Policy result = policyService.createPolicy(idType, idNumber, effectiveDateFrom, idPolicyType, idAffiliate, codeDesentralice, nameCompany);

        assertEquals(expectedPolicy, result);
        verify(policyDao).createPolicy(idType, idNumber, effectiveDateFrom, idPolicyType, idAffiliate, codeDesentralice, nameCompany);
    }

    @Test
    void testCreatePolicyDependent() {
        String idType = "TI";
        String idNumber = "67890";
        LocalDate effectiveDateFrom = LocalDate.now();
        Long idAffiliate = 3L;
        String code = "DEP";
        String nameCompany = "name company";
        Policy expectedPolicy = mock(Policy.class);


        when(policyDao.createPolicyDependent(idType, idNumber, effectiveDateFrom, idAffiliate, code, nameCompany))
                .thenReturn(expectedPolicy);

        Policy result = policyService.createPolicyDependent(idType, idNumber, effectiveDateFrom, idAffiliate, code, nameCompany);

        assertEquals(expectedPolicy, result);
        verify(policyDao).createPolicyDependent(idType, idNumber, effectiveDateFrom, idAffiliate, code, nameCompany);
    }

    @Test
    void testCreatePolicy_NullReturn() {
        when(policyDao.createPolicy(anyString(), anyString(), any(LocalDate.class), anyLong(), anyLong(), anyLong(), anyString()))
                .thenReturn(null);

        Policy result = policyService.createPolicy("CC", "00000", LocalDate.now(), 1L, 1L, 1L, "name");

        assertNull(result);
    }

    @Test
    void testCreatePolicyDependent_NullReturn() {
        when(policyDao.createPolicyDependent(anyString(), anyString(), any(LocalDate.class), anyLong(), anyString(), anyString()))
                .thenReturn(null);

        Policy result = policyService.createPolicyDependent("CC", "00000", LocalDate.now(), 1L, "CODE", "name");

        assertNull(result);
    }
}