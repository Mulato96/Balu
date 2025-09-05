package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneralNoveltyDTO {

    @JsonIgnore
    private Long id;
    private Long requestChannelId;
    private String requestChannelName;
    private String filedNumber;
    private LocalDate affiliationDate;
    private String noveltyType;
    private String status;
    private String observation;
    private Long idAffiliate;
}
