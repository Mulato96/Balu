package com.gal.afiliaciones.application.service.siarp.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gal.afiliaciones.application.service.siarp.SiarpAffiliateStatusGateway;
import com.gal.afiliaciones.application.service.siarp.SiarpStatusResult;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusRawClient;
import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SiarpAffiliateStatusGatewayImpl implements SiarpAffiliateStatusGateway {

    private final ConsultSiarpAffiliateStatusRawClient rawClient;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<SiarpStatusResult> getStatus(String tDocEmp, String idEmp, String tDocAfi, String idAfi) {
        return rawClient.consultRaw(tDocEmp, idEmp, tDocAfi, idAfi)
                .map(raw -> {
                    try {
                        ObjectMapper mapper = objectMapper.copy()
                                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                        List<TmpAffiliateStatusDTO> list = mapper.readValue(raw, new TypeReference<List<TmpAffiliateStatusDTO>>(){});
                        if (list != null) {
                            list.forEach(e -> e.setAppSource("SIARP"));
                            // Always include RAW; DTO is optional convenience
                            return SiarpStatusResult.ofBoth(list, raw);
                        }
                    } catch (Exception ex) {
                        log.debug("[SiarpAffiliateStatusGateway] DTO mapping failed; defaulting to RAW: {}", ex.getMessage());
                    }
                    return SiarpStatusResult.ofRaw(raw);
                });
    }
}


