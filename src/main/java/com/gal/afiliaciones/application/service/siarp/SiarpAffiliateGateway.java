package com.gal.afiliaciones.application.service.siarp;

import reactor.core.publisher.Mono;

public interface SiarpAffiliateGateway {
    Mono<SiarpAffiliateResult> getAffiliate(String tDoc, String idAfiliado);
}


