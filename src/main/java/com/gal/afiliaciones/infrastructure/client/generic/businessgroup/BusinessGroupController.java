package com.gal.afiliaciones.infrastructure.client.generic.businessgroup;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/business-group")
@RequiredArgsConstructor
public class BusinessGroupController {
    private final ConsultBusinessGroupClient consultBusinessGroupClient;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Mono<List<BusinessGroupResponse>> getGroups() {
        return consultBusinessGroupClient.getBusinessGroups();
    }
}
