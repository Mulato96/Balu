package com.gal.afiliaciones.application.service.siarp;

import com.gal.afiliaciones.infrastructure.dto.tmp.TmpAffiliateStatusDTO;

import java.util.List;
import java.util.Optional;

public class SiarpStatusResult {
    private final List<TmpAffiliateStatusDTO> dto;
    private final String rawJson;

    private SiarpStatusResult(List<TmpAffiliateStatusDTO> dto, String rawJson) {
        this.dto = dto;
        this.rawJson = rawJson;
    }

    public static SiarpStatusResult ofDto(List<TmpAffiliateStatusDTO> dto) {
        return new SiarpStatusResult(dto, null);
    }

    public static SiarpStatusResult ofRaw(String rawJson) {
        return new SiarpStatusResult(null, rawJson);
    }

    public static SiarpStatusResult ofBoth(List<TmpAffiliateStatusDTO> dto, String rawJson) {
        return new SiarpStatusResult(dto, rawJson);
    }

    public Optional<List<TmpAffiliateStatusDTO>> dto() {
        return Optional.ofNullable(dto);
    }

    public Optional<String> rawJson() {
        return Optional.ofNullable(rawJson);
    }
}


