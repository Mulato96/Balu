package com.gal.afiliaciones.infrastructure.client.generic.workerdisplacement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for worker displacement notification.
 * Used to notify when a worker is temporarily relocated to a different location.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkerDisplacementNotificationRequest {
    
    /**
     * Worker document type (e.g., "CC", "CE", "TI")
     */
    private String idTipoDoc;
    
    /**
     * Worker identification number
     */
    private String idPersona;
    
    /**
     * Employer document type (e.g., "NI", "CC")
     */
    private String idTipoDocEmp;
    
    /**
     * Employer identification number
     */
    private String idEmpresa;
    
    /**
     * Type of relationship/contract (1=Dependent, 2=Independent, etc.)
     */
    private Integer idTipoVinculacion;
    
    /**
     * Displacement start date (format: "yyyy-MM-dd")
     */
    private String fechaInicioDesp;
    
    /**
     * Displacement end date (format: "yyyy-MM-dd")
     */
    private String fechaFinDesp;
    
    /**
     * Department code of destination location
     */
    private Integer codigoDepartamento;
    
    /**
     * Municipality code of destination location
     */
    private Integer codigoMunicipio;
    
    /**
     * Reason for the displacement (free text)
     */
    private String motivoDesp;
}

