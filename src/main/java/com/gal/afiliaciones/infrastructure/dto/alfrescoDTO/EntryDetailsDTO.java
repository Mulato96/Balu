package com.gal.afiliaciones.infrastructure.dto.alfrescoDTO;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class EntryDetailsDTO {
    private String createdAt;
    @JsonProperty("isFolder")
    private boolean isFolder;
    @JsonProperty("isFile")
    private boolean isFile;
    private CreatedByUserDTO createdByUser;
    private String modifiedAt;
    private ModifiedByUserDTO modifiedByUser;
    private String name;
    private String id;
    private String nodeType;
    private ContentDTO content;
    private String parentId;

}

