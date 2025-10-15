package com.gal.afiliaciones.application.service.webhook.impl;

import com.gal.afiliaciones.application.service.affiliate.AffiliateService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.webhook.WebhookEmployerService;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.ConsultUserPortalClient;
import com.gal.afiliaciones.infrastructure.dao.repository.specifications.AffiliateMercantileSpecification;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import com.gal.afiliaciones.infrastructure.client.webhook.WebhookEmployerClient;
import com.gal.afiliaciones.infrastructure.dao.repository.affiliate.AffiliateMercantileRepository;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookEmployerServiceImpl implements WebhookEmployerService {

    private final ConsultUserPortalClient consultUserPortalClient;
    private final WebhookEmployerClient webhookEmployerClient;
    private final AffiliateMercantileRepository affiliateMercantileRepository;
    private final AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;
    private final AffiliateService affiliateService;

    private static final String PROCESS_DEPENDENT_INDEPENDENT = "👤 Procesando dependiente/independiente: {} - {}";
    private static final String PROCESS_EMPLOYER = "🏢 Procesando empleador para afiliación mercantil: {} - {}";

    @Override
    public List<WebhookEmployerResponseDTO> processEmployerAffiliation(String documentType, String username) {
        log.info("🚀 Iniciando procesamiento de afiliación webhook para usuario: {} - {}", documentType, username);
        
        try {
            // Obtener lista de empleadores del portal de usuario
            List<UserPortalResponse> userPortalList = consultUserPortalClient
                    .consult(documentType, username)
                    .block();
            
            if (userPortalList == null || userPortalList.isEmpty()) {
                log.info("📭 No se encontraron empleadores para el usuario: {} - {}", documentType, username);
                return new ArrayList<>();
            }
            
            // Convertir a formato webhook
            List<WebhookEmployerRequestDTO> webhookRequests = userPortalList.stream()
                    .map(this::convertToWebhookRequest)
                    .collect(Collectors.toList());
            
            log.info("📋 Procesando {} empleadores a través del webhook", webhookRequests.size());
            
            // Enviar al webhook
            @SuppressWarnings("unchecked")
            List<WebhookEmployerResponseDTO> webhookResponses = (List<WebhookEmployerResponseDTO>) webhookEmployerClient
                    .syncEmployers(webhookRequests)
                    .block();
            
            if (webhookResponses != null && !webhookResponses.isEmpty()) {
                log.info("✅ Webhook procesó {} empleadores exitosamente", webhookResponses.size());
                processWebhookResponses(webhookResponses, userPortalList);
            } else {
                log.warn("⚠️ El webhook no devolvió respuestas válidas");
            }
            
            return webhookResponses != null ? webhookResponses : new ArrayList<>();
            
        } catch (Exception e) {
            log.error("❌ Error procesando afiliación webhook para usuario {} - {}: {}", 
                    documentType, username, e.getMessage(), e);
            return new ArrayList<>();
        }
    }

    @Override
    public List<WebhookEmployerResponseDTO> processEmployersList(List<WebhookEmployerRequestDTO> employers) {
        log.info("🔄 Procesando lista directa de {} empleadores (uno por uno)", employers.size());
        List<WebhookEmployerResponseDTO> responses = new ArrayList<>();
        for (WebhookEmployerRequestDTO employer : employers) {
            try {
                WebhookEmployerResponseDTO response = webhookEmployerClient.syncEmployer(employer).block();
                if (response != null) {
                    responses.add(response);
                } else {
                    log.warn("⚠️ Webhook devolvió respuesta nula para empleador: {}", employer.getIdEmpresa());
                }
            } catch (Exception e) {
                log.error("❌ Error procesando empleador {}: {}", employer.getIdEmpresa(), e.getMessage(), e);
            }
        }
        log.info("✅ Lista de empleadores procesada uno por uno. Total respuestas: {}", responses.size());
        return responses;
    }

    @Override
    public WebhookEmployerRequestDTO convertToWebhookRequest(UserPortalResponse userPortal) {
        return WebhookEmployerRequestDTO.builder()
                .idTipoDocEmpresa(userPortal.getIdTipoDocEmpresa())
                .idEmpresa(userPortal.getIdEmpresa())
                .idSubEmpresa(userPortal.getIdSubEmpresa() != null ? userPortal.getIdSubEmpresa() : 0)
                .build();
    }

    /**
     * Procesa las respuestas del webhook y ejecuta la lógica de afiliación
     */
    private void processWebhookResponses(List<WebhookEmployerResponseDTO> webhookResponses, 
                                       List<UserPortalResponse> userPortalList) {
        log.info("🔧 Procesando respuestas del webhook para afiliación");
        
        if (webhookResponses == null || webhookResponses.isEmpty()) {
            log.warn("⚠️ No hay respuestas del webhook para procesar");
            return;
        }
        
        // Procesar el primer objeto (empleador) para afiliación mercantil
        WebhookEmployerResponseDTO firstResponse = webhookResponses.get(0);
        if (firstResponse.getEmpleador() != null) {
            log.info(PROCESS_EMPLOYER,
                    firstResponse.getEmpleador().getTipoDocumento(), 
                    firstResponse.getEmpleador().getNumeroDocumento());
            
            processEmployerForMercantile(firstResponse.getEmpleador(), userPortalList);
        } else {
            log.warn("⚠️ Primer objeto no contiene información de empleador");
        }
        
        // Procesar los objetos siguientes (dependientes/independientes)
        for (int i = 1; i < webhookResponses.size(); i++) {
            WebhookEmployerResponseDTO response = webhookResponses.get(i);
            if (response.getEmpleados() != null && !response.getEmpleados().isEmpty()) {
                for (WebhookEmployerResponseDTO.Dependiente dep : response.getEmpleados()) {
                    if (dep.getTipoDocumento() != null && dep.getNumeroDocumento() != null) {
                        log.info(PROCESS_DEPENDENT_INDEPENDENT, dep.getTipoDocumento(), dep.getNumeroDocumento());
                        processDependentOrIndependent(dep.getTipoDocumento(), dep.getNumeroDocumento());
                    } else {
                        log.warn("⚠️ Dependiente sin datos válidos en objeto {}", i);
                    }
                }
            } else {
                log.warn("⚠️ Objeto {} no contiene información válida de dependiente/independiente", i);
            }
        }
    }

    /**
     * Procesa las respuestas del webhook directamente (para Excel)
     */
    private void processWebhookResponsesDirectly(List<WebhookEmployerResponseDTO> webhookResponses) {
        log.info("🔧 Procesando respuestas del webhook directamente para afiliación");
        
        if (webhookResponses == null || webhookResponses.isEmpty()) {
            log.warn("⚠️ No hay respuestas del webhook para procesar");
            return;
        }
        
        // Procesar el primer objeto (empleador) para afiliación mercantil
        WebhookEmployerResponseDTO firstResponse = webhookResponses.get(0);
        if (firstResponse.getEmpleador() != null) {
            log.info(PROCESS_EMPLOYER,
                    firstResponse.getEmpleador().getTipoDocumento(), 
                    firstResponse.getEmpleador().getNumeroDocumento());
            
            processEmployerForMercantileDirectly(firstResponse.getEmpleador());
        } else {
            log.warn("⚠️ Primer objeto no contiene información de empleador");
        }
        
        // Procesar los objetos siguientes (dependientes/independientes)
        for (int i = 1; i < webhookResponses.size(); i++) {
            WebhookEmployerResponseDTO response = webhookResponses.get(i);
            if (response.getEmpleados() != null && !response.getEmpleados().isEmpty()) {
                for (WebhookEmployerResponseDTO.Dependiente dep : response.getEmpleados()) {
                    if (dep.getTipoDocumento() != null && dep.getNumeroDocumento() != null) {
                        log.info(PROCESS_DEPENDENT_INDEPENDENT, dep.getTipoDocumento(), dep.getNumeroDocumento());
                        processDependentOrIndependent(dep.getTipoDocumento(), dep.getNumeroDocumento());
                    } else {
                        log.warn("⚠️ Dependiente sin datos válidos en objeto {}", i);
                    }
                }
            } else {
                log.warn("⚠️ Objeto {} no contiene información válida de dependiente/independiente", i);
            }
        }
    }

    /**
     * Procesa el empleador para afiliación mercantil
     */
    private void processEmployerForMercantile(WebhookEmployerResponseDTO.EmployerData empleador, 
                                            List<UserPortalResponse> userPortalList) {
        try {
            String tipoDoc = empleador.getTipoDocumento();
            String idEmpresa = empleador.getNumeroDocumento();
            
            log.info(PROCESS_EMPLOYER, tipoDoc, idEmpresa);
            
            // Buscar el UserPortalResponse correspondiente
            UserPortalResponse matchingUserPortal = userPortalList.stream()
                    .filter(up -> up.getIdTipoDocEmpresa().equals(tipoDoc) && 
                                up.getIdEmpresa().equals(idEmpresa))
                    .findFirst()
                    .orElse(null);
            
            if (matchingUserPortal == null) {
                log.warn("⚠️ No se encontró UserPortalResponse para empleador: {} - {}", tipoDoc, idEmpresa);
                return;
            }
            
            // Verificar si ya existe afiliación mercantil
            List<AffiliateMercantile> affiliateMercantile = affiliateMercantileRepository.findAll(
                    AffiliateMercantileSpecification.findByNumberAndTypeDocumentAndDecentralizedBusinessName(
                            tipoDoc, idEmpresa, matchingUserPortal.getRazonSocial()
                    )
            );

            if (affiliateMercantile.isEmpty()) {
                log.info("📝 Creando nueva afiliación mercantil para empleador: {} - {}", tipoDoc, idEmpresa);
                
                // Afiliar empleador en mercantil
                affiliationEmployerActivitiesMercantileService.affiliateBUs(
                        matchingUserPortal.getIdTipoDocEmpresa(), 
                        matchingUserPortal.getIdEmpresa(), 
                        matchingUserPortal.getIdSubEmpresa()
                );
                
                log.info("✅ Empleador afiliado exitosamente en mercantil: {} - {}", tipoDoc, idEmpresa);
            } else {
                log.info("ℹ️ El empleador {} - {} ya tiene afiliación mercantil", tipoDoc, idEmpresa);
            }
            
        } catch (Exception e) {
            log.error("❌ Error procesando empleador para mercantil: {}", e.getMessage(), e);
        }
    }

    /**
     * Procesa dependientes/independientes para afiliación
     */
    private void processDependentOrIndependent(String tipoDoc, String numeroDoc) {
        try {
            log.info(PROCESS_DEPENDENT_INDEPENDENT, tipoDoc, numeroDoc);
            affiliateService.affiliateBUs(tipoDoc, numeroDoc);
            log.info("✅ Dependiente/independiente afiliado exitosamente: {} - {}", tipoDoc, numeroDoc);
        } catch (Exception e) {
            log.error("❌ Error afiliando dependiente/independiente [{} - {}]: {}", tipoDoc, numeroDoc, e.getMessage(), e);
        }
    }

    /**
     * Procesa el empleador para afiliación mercantil directamente (sin UserPortalResponse)
     */
    private void processEmployerForMercantileDirectly(WebhookEmployerResponseDTO.EmployerData empleador) {
        try {
            String tipoDoc = empleador.getTipoDocumento();
            String idEmpresa = empleador.getNumeroDocumento();
            
            log.info("🏢 Procesando empleador para afiliación mercantil directamente: {} - {}", tipoDoc, idEmpresa);
            
            // Verificar si ya existe afiliación mercantil
            List<AffiliateMercantile> affiliateMercantile = affiliateMercantileRepository.findAll(
                    AffiliateMercantileSpecification.findByNumberAndTypeDocument(tipoDoc, idEmpresa)
            );

            if (affiliateMercantile.isEmpty()) {
                log.info("📝 Creando nueva afiliación mercantil para empleador: {} - {}", tipoDoc, idEmpresa);
                
                // Afiliar empleador en mercantil usando los datos del webhook
                affiliationEmployerActivitiesMercantileService.affiliateBUs(tipoDoc, idEmpresa, 0);
                
                log.info("✅ Empleador afiliado exitosamente en mercantil: {} - {}", tipoDoc, idEmpresa);
            } else {
                log.info("ℹ️ El empleador {} - {} ya tiene afiliación mercantil", tipoDoc, idEmpresa);
            }
            
        } catch (Exception e) {
            log.error("❌ Error procesando empleador para mercantil directamente: {}", e.getMessage(), e);
        }
    }
} 