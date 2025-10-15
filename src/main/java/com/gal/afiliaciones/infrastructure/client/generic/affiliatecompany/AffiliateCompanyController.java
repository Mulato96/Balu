package com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@RequestMapping("/afiliado-postiva")
public class AffiliateCompanyController {

    private final ConsultAffiliateCompanyClient consultAffiliateCompanyClient;


    @GetMapping("/{tipoDoc}/{idAfiliado}")
    public Mono<ResponseEntity<AffiliateCompanyDto>> affiliateCompanyDto(
            @PathVariable String tipoDoc,
            @PathVariable String idAfiliado) {

        return consultAffiliateCompanyClient
                .consultAffiliate(tipoDoc, idAfiliado)          // Mono<List<AffiliateCompanyResponse>>
                .flatMapMany(Flux::fromIterable)                // Flux<AffiliateCompanyResponse>
                .filter(AffiliateCompanyController::isActive)   // solo activos
                .next()                                         // primer activo (Mono<AffiliateCompanyResponse>)
                .map(AffiliateCompanyController::toDto)         // map a DTO
                .map(ResponseEntity::ok)                        // 200 OK
                .defaultIfEmpty(ResponseEntity.notFound().build()); // 404 si no hay activo
    }

    // ---- Helpers ----
    private static boolean isActive(AffiliateCompanyResponse r) {
        String s = r.getEstadoRl();
        return s != null && s.trim().equalsIgnoreCase("Activo");
        // Si tu proveedor usa "Activa" o "A", podrías hacer:
        // return s != null && s.trim().toUpperCase().startsWith("ACTIV");
    }

    private static AffiliateCompanyDto toDto(AffiliateCompanyResponse r) {
        String name = joinNonNull(r.getNombre1(), r.getNombre2());
        String lastname = joinNonNull(r.getApellido1(), r.getApellido2());
        return new AffiliateCompanyDto(name, lastname);
    }

    private static String joinNonNull(String a, String b) {
        String s1 = a == null ? "" : a.trim();
        String s2 = b == null ? "" : b.trim();
        return (s1 + " " + s2).trim();
    }
}
