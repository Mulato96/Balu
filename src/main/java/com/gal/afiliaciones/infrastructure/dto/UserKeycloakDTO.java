package com.gal.afiliaciones.infrastructure.dto;



import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
public class UserKeycloakDTO {


    private String username;
    private String email;
    private String firsName;
    private String lastName;
    private String password;
    private Set<String> roles;
}
