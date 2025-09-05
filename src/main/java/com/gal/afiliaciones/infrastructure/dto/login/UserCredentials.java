package com.gal.afiliaciones.infrastructure.dto.login;

import com.gal.afiliaciones.infrastructure.enums.TypeUser;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;

@Getter
public class UserCredentials {
    private String typeDocument;
    private String username;
    private String password;
    @Enumerated(EnumType.STRING)
    private TypeUser typeUser;
}
