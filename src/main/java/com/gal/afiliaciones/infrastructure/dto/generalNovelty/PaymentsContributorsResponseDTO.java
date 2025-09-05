package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentsContributorsResponseDTO {
    private List<PaymentContributorDTO> content;
    private PageableDTO pageable;
    private Integer totalElements;
    private Boolean last;
    private Integer totalPages;
    private Boolean first;
    private Integer size;
    private Integer number;
    private SortDTO sort;
    private Integer numberOfElements;
    private Boolean empty;
} 