package com.gal.afiliaciones.infrastructure.dto.certificate;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataFileCertificateBulkDTO {

    private String idDocument;
    private LocalDateTime creationDate = LocalDateTime.now();
    private List<CertificateBulkDTO> listCertificateBulkDTO;
    private String documentNumberEmployer;
    private boolean isMassive = false;
    private BulkMassive bulkMassive = new BulkMassive();


    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    private static class BulkMassive{
        private String idDocumentAlfresco;
        private LocalDate startDate;
        private boolean failedCreateDocument = false;
        private long records;
        private String typeAffiliation;
    }

    public void createBulkMassive(String idDocumentAlfresco, LocalDate startDate, long records, String typeAffiliation){

        this.bulkMassive.idDocumentAlfresco = idDocumentAlfresco;
        this.bulkMassive.startDate = startDate;
        this.bulkMassive.records = records;
        this.bulkMassive.typeAffiliation = typeAffiliation;

    }

    public void setStatusDocument(){
        this.bulkMassive.failedCreateDocument = true;
    }

    public Map<String, Object> getBulkMassiveData() {
        Map<String, Object> response = new HashMap<>();
        response.put("idDocument", this.idDocument);
        response.put("Fecha solicitud", this.creationDate);
        response.put("Fecha Inicio Cobertura", this.bulkMassive.startDate);
        response.put("Estado", this.bulkMassive.failedCreateDocument);
        response.put("Total Certificados generados", this.bulkMassive.records);
        response.put("Tipo Afiliado", this.bulkMassive.typeAffiliation);
        return response;

    }

    public String getIdAlfresco(){
        return this.bulkMassive.idDocumentAlfresco;
    }
}
