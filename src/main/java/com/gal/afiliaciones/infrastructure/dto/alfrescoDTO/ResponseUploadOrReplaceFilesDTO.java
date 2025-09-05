package com.gal.afiliaciones.infrastructure.dto.alfrescoDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResponseUploadOrReplaceFilesDTO {

    private String idNewFolder;
    private List<ReplacedDocumentDTO> documents;

}
