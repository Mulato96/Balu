package com.gal.afiliaciones.application.service.webhook.impl;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.ConsultUserPortalClient;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import com.gal.afiliaciones.infrastructure.client.webhook.WebhookEmployerClient;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import reactor.core.publisher.Mono;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WebhookEmployerServiceImplTest {

    @Mock
    private ConsultUserPortalClient consultUserPortalClient;

    @Mock
    private WebhookEmployerClient webhookEmployerClient;

    @Mock
    private AffiliateMercantileRepository affiliateMercantileRepository;

    @Mock
    private AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;

    @Mock
    private AffiliateService affiliateService;

    @InjectMocks
    private WebhookEmployerServiceImpl webhookEmployerService;

    @Test
    void testProcessEmployerAffiliation_Success() {
        UserPortalResponse userPortalResponse = new UserPortalResponse();
        userPortalResponse.setIdTipoDocEmpresa("NI");
        userPortalResponse.setIdEmpresa("123456789");
        userPortalResponse.setRazonSocial("Test Company");

        WebhookEmployerResponseDTO webhookResponse = new WebhookEmployerResponseDTO();
        WebhookEmployerResponseDTO.EmployerData employerData = new WebhookEmployerResponseDTO.EmployerData();
        employerData.setTipoDocumento("NI");
        employerData.setNumeroDocumento("123456789");
        webhookResponse.setEmpleador(employerData);

        when(consultUserPortalClient.consult(anyString(), anyString())).thenReturn(Mono.just(List.of(userPortalResponse)));
        when(webhookEmployerClient.syncEmployers(anyList())).thenReturn(Mono.just(List.of(webhookResponse)));
        when(affiliateMercantileRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<WebhookEmployerResponseDTO> result = webhookEmployerService.processEmployerAffiliation("CC", "user");

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(affiliationEmployerActivitiesMercantileService).affiliateBUs(anyString(), anyString(), anyInt());
    }

    @Test
    void testProcessEmployerAffiliation_NoEmployersFound() {
        when(consultUserPortalClient.consult(anyString(), anyString())).thenReturn(Mono.just(Collections.emptyList()));

        List<WebhookEmployerResponseDTO> result = webhookEmployerService.processEmployerAffiliation("CC", "user");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testProcessEmployerAffiliation_WebhookError() {
        UserPortalResponse userPortalResponse = new UserPortalResponse();
        when(consultUserPortalClient.consult(anyString(), anyString())).thenReturn(Mono.just(List.of(userPortalResponse)));
        when(webhookEmployerClient.syncEmployers(anyList())).thenReturn(Mono.error(new RuntimeException("Webhook failed")));

        List<WebhookEmployerResponseDTO> result = webhookEmployerService.processEmployerAffiliation("CC", "user");

        assertNotNull(result);
        assertEquals(0, result.size());
    }

    @Test
    void testProcessEmployersList_Success() {
        WebhookEmployerRequestDTO request = new WebhookEmployerRequestDTO();
        WebhookEmployerResponseDTO response = new WebhookEmployerResponseDTO();
        when(webhookEmployerClient.syncEmployer(any(WebhookEmployerRequestDTO.class))).thenReturn(Mono.just(response));

        List<WebhookEmployerResponseDTO> result = webhookEmployerService.processEmployersList(List.of(request));

        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testConvertToWebhookRequest() {
        UserPortalResponse userPortal = new UserPortalResponse();
        userPortal.setIdTipoDocEmpresa("NI");
        userPortal.setIdEmpresa("123");
        userPortal.setIdSubEmpresa(456);

        WebhookEmployerRequestDTO result = webhookEmployerService.convertToWebhookRequest(userPortal);

        assertEquals("NI", result.getIdTipoDocEmpresa());
        assertEquals("123", result.getIdEmpresa());
        assertEquals(456, result.getIdSubEmpresa());
    }
}
