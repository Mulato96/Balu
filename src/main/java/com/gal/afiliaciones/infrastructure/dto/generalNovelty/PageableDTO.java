package com.gal.afiliaciones.infrastructure.dto.generalNovelty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageableDTO {
    private Integer pageNumber;
    private Integer pageSize;
    private SortDTO sort;
    private Integer offset;
    private Boolean paged;
    private Boolean unpaged;
} 