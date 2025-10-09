package com.gal.afiliaciones.infrastructure.quartz;

import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliate.Affiliate;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.domain.model.retirement.RetirementRepository;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
public class RetirementJob implements Job {

    @Autowired
    private RetirementRepository retirementRepository;

    @Autowired
    private AffiliateRepository affiliateRepository;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        List<Retirement> retirements = retirementRepository.findAllByRetirementDate(LocalDate.now());
        retirements.forEach(this::processRetirement);
    }

    private void processRetirement(Retirement retirement) {
        Affiliate affiliate = affiliateRepository.findById(retirement.getIdAffiliate()).orElse(null);
        if (affiliate != null) {
            affiliate.setAffiliationStatus("Retirado");
            affiliate.setRetirementDate(retirement.getRetirementDate());
            affiliateRepository.save(affiliate);
        }
    }
}