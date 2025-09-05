package com.gal.afiliaciones.config.ex.workspaceofficial;

import com.gal.afiliaciones.config.ex.AffiliationsExceptionBase;
import com.gal.afiliaciones.config.ex.Error;
import org.springframework.http.HttpStatus;

public class WorkspaceOptionOfficialException extends AffiliationsExceptionBase {

    public WorkspaceOptionOfficialException(String message) {
        super(Error.builder()
                .type(Error.Type.ERROR_OPTIONS_WORKSPACE_OFFICIAL)
                .message(message)
                .build(), HttpStatus.BAD_REQUEST);
    }

}
