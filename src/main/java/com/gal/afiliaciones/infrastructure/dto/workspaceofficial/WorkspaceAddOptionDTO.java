package com.gal.afiliaciones.infrastructure.dto.workspaceofficial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceAddOptionDTO {

    private Long idModule;
    private Long idOption;
    private Long idOfficial;
    private String nameImage;

}
