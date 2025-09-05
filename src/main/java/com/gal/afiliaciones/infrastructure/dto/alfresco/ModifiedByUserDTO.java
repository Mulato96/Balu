package com.gal.afiliaciones.infrastructure.dto.alfresco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ModifiedByUserDTO {
    private String id;
    private String displayName;
}
