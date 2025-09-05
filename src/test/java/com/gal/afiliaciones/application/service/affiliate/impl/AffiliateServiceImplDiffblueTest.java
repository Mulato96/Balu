package com.gal.afiliaciones.application.service.affiliate.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.gal.afiliaciones.application.service.GenerateCardAffiliatedService;
import com.gal.afiliaciones.application.service.KeycloakService;
import com.gal.afiliaciones.application.service.RolesUserService;
import com.gal.afiliaciones.application.service.affiliationemployerdomesticserviceindependent.SendEmails;
import com.gal.afiliaciones.application.service.alfresco.AlfrescoService;
import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.application.service.documentnamestandardization.DocumentNameStandardizationService;
import com.gal.afiliaciones.application.service.policy.PolicyService;
import com.gal.afiliaciones.config.ex.certificate.AffiliateNotFoundException;
import com.gal.afiliaciones.config.util.CollectProperties;
import com.gal.afiliaciones.domain.model.affiliate.EmployerSize;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.domain.model.affiliationemployerdomesticserviceindependent.Affiliation;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.dao.repository.Certificate.AffiliateRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.DateInterviewWebRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationCancellationTimerRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IAffiliationEmployerDomesticServiceIndependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IDataDocumentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.IUserPreRegisterRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.MunicipalityRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.EmployerSizeRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.RequestChannelRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdependent.AffiliationDependentRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliationdetail.AffiliationDetailRepository;
import com.gal.afiliaciones.infrastructure.dao.repository.eps.HealthPromotingEntityRepository;
import com.gal.afiliaciones.infrastructure.utils.Constant;
import com.gal.afiliaciones.infrastructure.utils.KeyCloakProvider;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ContextConfiguration(classes = {AffiliateServiceImpl.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class AffiliateServiceImplDiffblueTest {
    @MockBean
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @MockBean
    private AffiliateRepository affiliateRepository;

    @Autowired
    private AffiliateServiceImpl affiliateServiceImpl;

    @MockBean
    private AffiliationDependentRepository affiliationDependentRepository;

    @MockBean
    private AffiliationDetailRepository affiliationDetailRepository;

    @MockBean
    private AlfrescoService alfrescoService;

    @MockBean
    private CollectProperties collectProperties;

    @MockBean
    private DailyService dailyService;

    @MockBean
    private DateInterviewWebRepository dateInterviewWebRepository;

    @MockBean
    private GenerateCardAffiliatedService generateCardAffiliatedService;

    @MockBean
    private GenericWebClient genericWebClient;

    @MockBean
    private IAffiliationCancellationTimerRepository iAffiliationCancellationTimerRepository;

    @MockBean
    private IAffiliationEmployerDomesticServiceIndependentRepository iAffiliationEmployerDomesticServiceIndependentRepository;

    @MockBean
    private IDataDocumentRepository iDataDocumentRepository;

    @MockBean
    private IUserPreRegisterRepository iUserPreRegisterRepository;

    @MockBean
    private KeyCloakProvider keyCloakProvider;

    @MockBean
    private KeycloakService keycloakService;

    @MockBean
    private PolicyService policyService;

    @MockBean
    private RequestChannelRepository requestChannelRepository;

    @MockBean
    private RolesUserService rolesUserService;

    @MockBean
    private SendEmails sendEmails;

    @MockBean
    private DocumentNameStandardizationService documentNameStandardizationService;

    @MockBean
    private EmployerSizeRepository employerSizeRepository;
    @MockBean
    private InsertPersonClient insertPersonClient;
    @MockBean
    private MunicipalityRepository municipalityRepository;
    @MockBean
    private HealthPromotingEntityRepository epsRepository;

}
