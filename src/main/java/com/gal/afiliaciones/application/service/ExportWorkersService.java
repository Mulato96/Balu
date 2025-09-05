package com.gal.afiliaciones.application.service;

import com.gal.afiliaciones.infrastructure.dto.ExportDocumentsDTO;

public interface ExportWorkersService {

    /**
     * Exporta los trabajadores dependientes e independientes de un empleador por NIT
     * @param nit NIT del empleador
     * @param exportType tipo de exportación (xlsx, csv, etc.)
     * @return DTO con la información del archivo exportado
     */
    ExportDocumentsDTO exportAllWorkersByNit(String nit, String exportType);
}
