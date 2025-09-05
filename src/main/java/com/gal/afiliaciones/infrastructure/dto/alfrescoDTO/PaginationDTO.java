package com.gal.afiliaciones.infrastructure.dto.alfrescoDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PaginationDTO {
    private int count;
    private boolean hasMoreItems;
    private int totalItems;
    private int skipCount;
    private int maxItems;
}
