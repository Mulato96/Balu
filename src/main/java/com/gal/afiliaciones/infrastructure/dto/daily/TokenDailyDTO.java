package com.gal.afiliaciones.infrastructure.dto.daily;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TokenDailyDTO {

    private String name;
    private Long idOfficial;
    private LocalDateTime start;
    private LocalDateTime end;
    private Long idRoom;
}
