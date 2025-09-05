package com.gal.afiliaciones.infrastructure.client.generic;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TokenClientResponse {
    private String access_token;
    private String token_type;
    private Integer expires_in;
}
