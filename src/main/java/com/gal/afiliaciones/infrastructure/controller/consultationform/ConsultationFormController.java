package com.gal.afiliaciones.infrastructure.controller.consultationform;


import com.gal.afiliaciones.application.service.consultationform.ConsultEmployerInfo;
import com.gal.afiliaciones.application.service.consultationform.ConsultationFormService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.domain.model.HistoryOptions;
import com.gal.afiliaciones.infrastructure.dto.consultationform.*;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.ContractsJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryAffiliationsWithdrawalsHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.HistoryJobRelatedDTO;
import com.gal.afiliaciones.infrastructure.dto.consultationform.infoworker.UpdatesWorkerHistoryDTO;
import com.gal.afiliaciones.infrastructure.dto.retirementreason.RegisteredAffiliationsDTO;
import com.gal.afiliaciones.infrastructure.dto.workerdetail.WorkerDetailDTO;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@RestController
@RequestMapping("/consultationForm")
@AllArgsConstructor
public class ConsultationFormController {

    private final ConsultationFormService service;
    private final ConsultEmployerInfo consultEmployerInfo;

    @Operation(description = "Consultar formulario de afiliacion trabajdor o empleador")
    @GetMapping("getInfo")
    public ResponseEntity<BodyResponseConfig<InfoConsultDTO>> getInfo(@Param("identification") String identification,
                                                                      @Param("typeIdentification") String typeIdentification,
                    @Param("affiliationType") String affiliationType,
                    @Param("idAffiliate") String idAffiliate) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getInfo(typeIdentification, identification, affiliationType, idAffiliate), "info"), HttpStatus.OK);
    }

    @GetMapping("getJobRelatedInfo")
    public ResponseEntity<BodyResponseConfig<List<JobRelationShipDTO>>> getJobRelatedInfo(
            @Param("identification") String identification, @Param("typeIdentification") String typeIdentification) {

        return new ResponseEntity<>(
                new BodyResponseConfig<>(
                        service.getJobRelatedInfo(typeIdentification, identification), "info"), HttpStatus.OK);
    }

    @GetMapping("historyAffiliationsWithdrawals")
    public ResponseEntity<BodyResponseConfig<List<HistoryAffiliationsWithdrawalsDTO>>> historyAffiliationsWithdrawals(
            @Param("identification") String identification, @Param("typeIdentification") String typeIdentification) {

        return new ResponseEntity<>(
                new BodyResponseConfig<>(
                        service.getHistoryAffiliationsWithdrawals(typeIdentification, identification), "info history retirement"), HttpStatus.OK);
    }

    @GetMapping("getUpdatesWorker")
    public ResponseEntity<BodyResponseConfig<List<ConsultUpdatesDTO>>> consultUpdates(
            @Param("identification") String identification, @Param("typeIdentification") String typeIdentification) {

        return new ResponseEntity<>(
                new BodyResponseConfig<>(
                        service.consultUpdates(typeIdentification, identification), "info updates employee"), HttpStatus.OK);
    }

    //info del empleador
    @GetMapping("getInfoPoliceEmployer")
    public ResponseEntity<BodyResponseConfig<List<PolicyDTO>>> getInfoEmployer(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getPolicyInfo(filedNumber), "info polizas"), HttpStatus.OK);
    }

    @GetMapping("getEconomoyActivitiesEmployer")
    public ResponseEntity<BodyResponseConfig<List<RegisteredAffiliationsDTO>>> getEconomyActivities(@Param("identification") String identification,
                                                                                                    @Param("typeIdentification") String typeIdentification) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getEconomyActivities(typeIdentification, identification), "info actividades economicas"), HttpStatus.OK);
    }

    @GetMapping("getHistoryOptions")
    public ResponseEntity<BodyResponseConfig<List<HistoryOptions>>> getHistoryOptions() {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getHistoryOptions(), "info opciones del historial"), HttpStatus.OK);
    }

    //history
    @GetMapping("getAffiliationInfoEmployer")
    public ResponseEntity<BodyResponseConfig<AffiliationInformationDTO>> getAffiliationInfoEmployer(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getAffiliationInfoEmployeer(filedNumber), "info afiliacaion del empleador"), HttpStatus.OK);
    }

    @GetMapping("getNoveltyWebInfo")
    public ResponseEntity<BodyResponseConfig<List<EmployerUpdateDTO>>> getUpdatesWeb(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getUpdatesWeb(filedNumber), "info novedades webs de la afiliacion"), HttpStatus.OK);
    }

    @GetMapping("getSedesInfo")
    public ResponseEntity<BodyResponseConfig<List<HeadquartersAffiliationDTO>>> getHeadquarters(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getHeadquarters(filedNumber), "info sedes de la afiliacion"), HttpStatus.OK);
    }

    @GetMapping("getDocumentsAffiliationEmployer")
    public ResponseEntity<BodyResponseConfig<DocumentsOfAffiliationDTO>> getDocumentAffiliation(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getDocumentsAffiliation(filedNumber), "info docuemntos asociados a afiliacion"), HttpStatus.OK);
    }

    @GetMapping("getDocumentsCollection")
    public ResponseEntity<BodyResponseConfig<DocumentsCollectionAffiliationDTO>> getDocumentsCollection(@Param("identification") String identification,
                                                                                                        @Param("typeIdentification") String typeIdentification) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(consultEmployerInfo.getDcoumentsColection(typeIdentification,  identification), "info docuemntos generados en recaudo"), HttpStatus.OK);
    }

    //acciones trabajadores
    @GetMapping("getJobRelatedInfoHistory")
    public ResponseEntity<BodyResponseConfig<HistoryJobRelatedDTO>> getHistoryJobRelated(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getHistoryJobRelated(filedNumber), "info historial de Relacion laboral"), HttpStatus.OK);
    }

    @GetMapping("getJobRelatedInfoContracts")
    public ResponseEntity<BodyResponseConfig<List<ContractsJobRelatedDTO>>> getContractsJobRelated(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getContractsJobRelated(filedNumber), "info contratos de relacion laboral"), HttpStatus.OK);
    }

    @GetMapping("getHistoryAffiliationsWithdrawalsHistory")
    public ResponseEntity<BodyResponseConfig<HistoryAffiliationsWithdrawalsHistoryDTO>> getAffiliationWithdrawalsHistory(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getAffiliationWithdrawalsHistory(filedNumber), "info historial de historial afiliaciones y retiros"), HttpStatus.OK);
    }

    @GetMapping("getUpdatesWorkerHistory")
    public ResponseEntity<BodyResponseConfig<UpdatesWorkerHistoryDTO>> getUpdatesWorkerHistory(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getUpdatesWorkerHistory(filedNumber), "info historial de novedades trabajdor"), HttpStatus.OK);
    }

    @GetMapping("getDocumentsAffiliationWorker")
    public ResponseEntity<BodyResponseConfig<DocumentsOfAffiliationDTO>> getDocumentAffiliationWorker(@Param("filedNumber") String filedNumber) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getDocumentAffiliationWorker(filedNumber), "info docuemntos asociados a afiliacion"), HttpStatus.OK);
    }

    @Operation(description = "Consulta general del trabajdor o empleador")
    @GetMapping("generalConsult")
    public ResponseEntity<BodyResponseConfig<List<GeneralConsultDTO>>> generalConsult(
            @Param("identification") String identification,
            @Param("typeIdentification") String typeIdentification) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.generalConsult(typeIdentification, identification), "info"), HttpStatus.OK);
    }

    @Operation(description = "Consultar detalle para un trabajador")
    @GetMapping("detailWorkerConsult/{filedNumber}")
    public ResponseEntity<BodyResponseConfig<WorkerDetailDTO>> detailWorkerConsult(
            @PathVariable("filedNumber") String filedNumber
            ) {
        return new ResponseEntity<>(
                new BodyResponseConfig<>(service.getWorkerDetails(filedNumber), "info"), HttpStatus.OK);
    }

}
