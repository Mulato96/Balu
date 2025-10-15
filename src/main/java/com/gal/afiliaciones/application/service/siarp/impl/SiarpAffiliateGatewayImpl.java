package com.gal.afiliaciones.application.service.siarp.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateGateway;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateResult;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateRawClient;
import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiarpAffiliateGatewayImpl implements SiarpAffiliateGateway {

    private final ConsultSiarpAffiliateRawClient rawClient;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<SiarpAffiliateResult> getAffiliate(String tDoc, String idAfiliado) {
        return rawClient.consultRaw(tDoc, idAfiliado)
                .map(raw -> {
                    try {
                        ObjectMapper mapper = objectMapper.copy()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        List<EmployerEmployeeDTO> list = mapper.readValue(raw, new TypeReference<List<EmployerEmployeeDTO>>(){});
                        if (list != null) {
                            list.forEach(e -> e.setAppSource("SIARP"));
                            return SiarpAffiliateResult.ofBoth(list, raw);
                        }
                    } catch (Exception ex) {
                        log.debug("[SiarpAffiliateGateway] DTO mapping failed; defaulting to RAW: {}", ex.getMessage());
                    }
                    return SiarpAffiliateResult.ofRaw(raw);
                });
    }
}


