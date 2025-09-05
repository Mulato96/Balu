package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NoveltyContributorResponseDTO {
    private String channelName;
    private LocalDateTime receivedDate;
    private String contributantIdentification;
    private String contributantFullName;
    private String noveltyType;
    private String status;
    private String causal;
}
