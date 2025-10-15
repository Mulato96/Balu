package com.gal.afiliaciones.application.service.siarp;

import com.gal.afiliaciones.infrastructure.dto.employer.EmployerEmployeeDTO;

import java.util.List;
import java.util.Optional;

public class SiarpAffiliateResult {
    private final List<EmployerEmployeeDTO> dto;
    private final String rawJson;

    private SiarpAffiliateResult(List<EmployerEmployeeDTO> dto, String rawJson) {
        this.dto = dto;
        this.rawJson = rawJson;
    }

    public static SiarpAffiliateResult ofDto(List<EmployerEmployeeDTO> dto) {
        return new SiarpAffiliateResult(dto, null);
    }

    public static SiarpAffiliateResult ofRaw(String rawJson) {
        return new SiarpAffiliateResult(null, rawJson);
    }

    public static SiarpAffiliateResult ofBoth(List<EmployerEmployeeDTO> dto, String rawJson) {
        return new SiarpAffiliateResult(dto, rawJson);
    }

    public Optional<List<EmployerEmployeeDTO>> dto() { return Optional.ofNullable(dto); }
    public Optional<String> rawJson() { return Optional.ofNullable(rawJson); }
}


