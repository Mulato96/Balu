package com.gal.afiliaciones.application.service.siarp;

import reactor.core.publisher.Mono;

public interface SiarpAffiliateStatusGateway {
    Mono<SiarpStatusResult> getStatus(String tDocEmp, String idEmp, String tDocAfi, String idAfi);
}


