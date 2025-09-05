package com.gal.afiliaciones.application.service.risk.impl;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.gal.afiliaciones.domain.model.RiskFee;
import com.gal.afiliaciones.infrastructure.dao.repository.risk.RiskFeeDao;



class RiskFeeServiceImpTest {

    private RiskFeeDao riskFeeDao;
    private RiskFeeServiceImp riskFeeServiceImp;

    @BeforeEach
    void setUp() {
        riskFeeDao = mock(RiskFeeDao.class);
        riskFeeServiceImp = new RiskFeeServiceImp(riskFeeDao);
    }

    @Test
    void getFeeByRisk_returnsFeeWhenRiskFeeExists() {
        String risk = "HIGH";
        RiskFee riskFee = new RiskFee();
        riskFee.setFee(new BigDecimal("123.45"));

        when(riskFeeDao.getFeeByRisk(risk)).thenReturn(riskFee);

        BigDecimal fee = riskFeeServiceImp.getFeeByRisk(risk);

        assertEquals(new BigDecimal("123.45"), fee);
        verify(riskFeeDao).getFeeByRisk(risk);
    }

    @Test
    void getFeeByRisk_returnsZeroWhenRiskFeeDoesNotExist() {
        String risk = "LOW";

        when(riskFeeDao.getFeeByRisk(risk)).thenReturn(null);

        BigDecimal fee = riskFeeServiceImp.getFeeByRisk(risk);

        assertEquals(BigDecimal.ZERO, fee);
        verify(riskFeeDao).getFeeByRisk(risk);
    }

    @Test
    void getAllRiskFee_returnsListFromDao() {
        RiskFee rf1 = new RiskFee();
        RiskFee rf2 = new RiskFee();
        List<RiskFee> riskFees = Arrays.asList(rf1, rf2);

        when(riskFeeDao.findAll()).thenReturn(riskFees);

        List<RiskFee> result = riskFeeServiceImp.getAllRiskFee();

        assertSame(riskFees, result);
        verify(riskFeeDao).findAll();
    }
}