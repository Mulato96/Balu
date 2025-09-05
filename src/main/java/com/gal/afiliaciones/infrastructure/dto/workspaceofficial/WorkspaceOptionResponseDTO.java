package com.gal.afiliaciones.infrastructure.dto.workspaceofficial;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WorkspaceOptionResponseDTO {

    private Long id;
    private String moduleName;
    private String optionName;
    private String nameImage;

}
