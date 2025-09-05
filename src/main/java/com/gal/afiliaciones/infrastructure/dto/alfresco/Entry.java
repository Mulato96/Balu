package com.gal.afiliaciones.infrastructure.dto.alfresco;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Entry {
    private String createdAt;
    private boolean isFolder;
    private boolean isFile;
    private CreatedByUser createdByUser;
    private String modifiedAt;
    private ModifiedByUserDTO modifiedByUser;
    private String name;
    private String id;
    private String nodeType;
    private Content content;
    private String parentId;

}
