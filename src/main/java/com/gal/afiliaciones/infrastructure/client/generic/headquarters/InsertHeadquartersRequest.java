package com.gal.afiliaciones.infrastructure.client.generic.headquarters;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsertHeadquartersRequest {
    // Employer identification
    private String tipoDocEmp;          // Document type (NIT, CC, etc.)
    private String numeDocEmp;          // Document number
    private Integer subempresa;         // Sub-company ID (usually 0 or 1)
    
    // Location
    private Integer idDepartamento;     // Department ID
    private Integer idMunicipio;        // Municipality ID (converted)
    
    // Economic activity
    private Integer idActEconomica;     // Economic activity ID
    
    // Headquarters information
    private Integer principal;          // 1 = main office, 0 = branch
    private String fechaRadicacion;     // Filing date (YYYY-MM-DD)
    private String nombre;              // Office name
    private String direccion;           // Full address
    private String zona;                // Zone: "U" (Urban) or "R" (Rural)
    private String telefono;            // Office phone
    private String email;               // Office email
    
    // Responsible person
    private String tipoDocResp;         // Responsible person doc type
    private String numeDocResp;         // Responsible person doc number
    
    // Mission headquarters (temporal workers)
    private Integer sedeMision;         // 1 = mission headquarters, 0 = normal
    private String tipoDocEmpMision;    // Mission employer doc type
    private String numeDocEmpMision;    // Mission employer doc number
    
    // Workers
    private Integer numeroTrab;         // Number of workers
}

