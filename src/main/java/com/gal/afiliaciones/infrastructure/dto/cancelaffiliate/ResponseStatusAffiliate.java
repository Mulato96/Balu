package com.gal.afiliaciones.infrastructure.dto.cancelaffiliate;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResponseStatusAffiliate {
    private String status;
}
