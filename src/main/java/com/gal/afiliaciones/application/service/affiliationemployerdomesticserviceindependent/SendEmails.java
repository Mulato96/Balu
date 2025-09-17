package com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent;

import com.gal.afiliaciones.domain.model.UserMain;
import com.gal.afiliaciones.domain.model.affiliationdependent.AffiliationDependent;
import com.gal.afiliaciones.domain.model.Retirement;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DataEmployerDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.TemplateSendEmailsDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.novelty.DataEmailNotApplyDTO;
import com.gal.afiliaciones.infrastructure.dto.workermanagement.DataEmailUpdateEmployerDTO;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Map;

public interface SendEmails {

    void requestDenied(Affiliation affiliation, StringBuilder observation);
    void requestAccepted(Affiliation affiliation);
    void welcome(Affiliation affiliation, Long idAffiliate, String affiliationType, String subtypeAffiliate);
    void requestDeniedDocumentsMercantile(TemplateSendEmailsDTO templateSendEmailsDTO, StringBuilder observation);
    void interviewWeb(TemplateSendEmailsDTO templateSendEmailsDTO);
    void confirmationInterviewWeb(TemplateSendEmailsDTO templateSendEmailsDTO);
    void confirmationInterviewWebOfficial(LocalDateTime dateInterview, String email, String filedNumber);
    void reminderInterviewWeb(TemplateSendEmailsDTO templateSendEmailsDTO);
    void interviewWebApproved(TemplateSendEmailsDTO templateSendEmailsDTO);
    void welcomeMercantile(TemplateSendEmailsDTO templateSendEmailsDTO);
    void welcomeDependent(AffiliationDependent affiliation, Long idAffiliate, DataEmployerDTO dataEmployerDTO, Long idBondingType);
    void emailUpdateDependent(AffiliationDependent affiliation, DataEmployerDTO dataEmployerDTO);
    void emailUpdateEmployer(DataEmailUpdateEmployerDTO dataEmail);
    void emailUpdateMassiveWorkers(MultipartFile file, DataEmailUpdateEmployerDTO dataEmail);
    void emailWorkerRetirement(Retirement workerRetirement, String emailEmployer, String completeNameEmployer);
    void emailBulkLoad(String company, String email, MultipartFile file);
    void sendEmailHeadquarters(Map<String, Object> data, String email);
    void emailWelcomeRegister(UserMain user);
    void emailApplyPILA(DataEmailApplyDTO dataEmail);
    void emailNotApplyPILA(DataEmailNotApplyDTO dataEmail);
    void emailNotRetirementPILA(Map<String , Object> data, String email);
    void emailCertificateMassive(LocalDateTime dateRequest, String email);
}