package com.gal.afiliaciones.infrastructure.controller.integration;

import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.InsertPersonRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.UpdatePersonClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.UpdatePersonRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonRequest;

import com.gal.afiliaciones.infrastructure.client.generic.sat.v2.SatConsultTransferableEmployerClientV2;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerRequest;
import com.gal.afiliaciones.infrastructure.client.generic.novelty.WorkerRetirementNoveltyClient;
import com.gal.afiliaciones.infrastructure.client.generic.novelty.WorkerRetirementNoveltyRawClient;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyRequest;
import com.gal.afiliaciones.infrastructure.dto.novelty.WorkerRetirementNoveltyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.InsertLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.InsertLegalRepresentativeRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeRequest;
import com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship.DependentRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship.DependentRelationshipRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.dependentrelationship.DependentRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateClient;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusClient;
import com.gal.afiliaciones.infrastructure.client.generic.siarp.ConsultSiarpAffiliateStatusRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.InsertEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.InsertEmployerRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.UpdateEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.UpdateEmployerRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerRequest;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.ConsultEmployerRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.employer.EmployerResponse;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.InsertPolicyRequest;
import com.gal.afiliaciones.infrastructure.client.generic.policy.ConsultPolicyClient;
import com.gal.afiliaciones.infrastructure.client.generic.policy.ConsultPolicyRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.branch.ConsultBranchClient;
import com.gal.afiliaciones.infrastructure.client.generic.branch.ConsultBranchRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.ConsultHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.ConsultHeadquartersRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.InsertHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.InsertHeadquartersRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.InsertHeadquartersRequest;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.UpdateHeadquartersClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.UpdateHeadquartersRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.headquarters.UpdateHeadquartersRequest;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.InsertWorkCenterRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.WorkCenterRequest;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.ConsultWorkCenterClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.ConsultWorkCenterRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.UpdateWorkCenterClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.UpdateWorkCenterRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.workcenter.UpdateWorkCenterRequest;
import com.gal.afiliaciones.infrastructure.client.generic.workerposition.UpdateWorkerPositionClient;
import com.gal.afiliaciones.infrastructure.client.generic.workerposition.UpdateWorkerPositionRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.workerposition.UpdateWorkerPositionRequest;
import com.gal.afiliaciones.infrastructure.client.generic.workerdisplacement.WorkerDisplacementNotificationClient;
import com.gal.afiliaciones.infrastructure.client.generic.workerdisplacement.WorkerDisplacementNotificationRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.workerdisplacement.WorkerDisplacementNotificationRequest;
import com.gal.afiliaciones.infrastructure.client.generic.independentactivity.UpdateIndependentEconomicActivityClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentactivity.UpdateIndependentEconomicActivityRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentactivity.UpdateIndependentEconomicActivityRequest;
import com.gal.afiliaciones.infrastructure.client.generic.independentcontract.UpdateIndependentContractDateClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentcontract.UpdateIndependentContractDateRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentcontract.UpdateIndependentContractDateRequest;
import com.gal.afiliaciones.infrastructure.client.generic.independentoccupation.UpdateIndependentOccupationClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentoccupation.UpdateIndependentOccupationRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentoccupation.UpdateIndependentOccupationRequest;
import com.gal.afiliaciones.infrastructure.client.generic.employeractivities.ConsultEmployerActivitiesClient;
import com.gal.afiliaciones.infrastructure.client.generic.employeractivities.ConsultEmployerActivitiesRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.employeractivation.ConsultEmployerActivationClient;
import com.gal.afiliaciones.infrastructure.client.generic.employeractivation.ConsultEmployerActivationRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.independentrelationship.IndependentContractRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.webhook.WebhookEmployerClient;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.webhook.WebhookEmployerResponseDTO;
import com.gal.afiliaciones.infrastructure.client.registraduria.RegistraduriaClient;
import com.gal.afiliaciones.infrastructure.client.confecamaras.ConfecamarasClient;
import com.gal.afiliaciones.infrastructure.dto.wsconfecamaras.RecordResponseDTO;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipClient;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.volunteer.VolunteerRelationshipRequest;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.ConsultUserPortalClient;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.ConsultUserPortalRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.userportal.UserPortalResponse;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonlClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.ConsultPersonRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.person.PersonResponse;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.ConsultLegalRepresentativeClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.ConsultLegalRepresentativeRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.legalrepresentative.LegalRepresentativeResponse;
import com.gal.afiliaciones.infrastructure.client.generic.businessgroup.ConsultBusinessGroupClient;
import com.gal.afiliaciones.infrastructure.client.generic.businessgroup.ConsultBusinessGroupRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.businessgroup.BusinessGroupResponse;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.ConsultAffiliateCompanyRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.affiliatecompany.AffiliateCompanyResponse;
import com.gal.afiliaciones.infrastructure.client.generic.recaudos.ConsultContributorDataClient;
import com.gal.afiliaciones.infrastructure.client.generic.recaudos.ConsultContributorDataRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.recaudos.ConsultMemberDataClient;
import com.gal.afiliaciones.infrastructure.client.generic.recaudos.ConsultMemberDataRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.recaudos.ConsultBankLogClient;
import com.gal.afiliaciones.infrastructure.client.generic.recaudos.ConsultBankLogRawClient;
import com.gal.afiliaciones.infrastructure.client.generic.GenericWebClient;
import com.gal.afiliaciones.infrastructure.dto.certificate.CertificateReportRequestDTO;
import com.gal.afiliaciones.infrastructure.dto.UserDtoApiRegistry;
import com.gal.afiliaciones.infrastructure.dto.salary.SalaryDTO;
import com.gal.afiliaciones.infrastructure.dto.RegistryOfficeDTO;
import com.gal.afiliaciones.domain.model.Occupation;
import com.gal.afiliaciones.infrastructure.dto.sat.TransferableEmployerResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Test controller for integrations v2 functionality.
 * Tests the clean v2 SAT client with automatic HTTP call tracking.
 */
@RestController
@RequestMapping("/api/v1/integrations-v2/test")
@Tag(name = "Integrations v2 Test", description = "Test SAT v2 client with automatic telemetry")
@RequiredArgsConstructor
@Slf4j
public class IntegrationsV2TestController {

    private static final String ID_AFILIADO_PARAM = "idAfiliado";
    private static final String TIPO_DOC_PARAM = "tipoDoc";
    private static final String ID_TIPO_DOC_PARAM = "idTipoDoc";
    private static final String ID_EMPRESA_PARAM = "idEmpresa";
    private static final String ID_SUB_EMPRESA_PARAM = "idSubEmpresa";
    private static final String TYPE_DOC_APO = "tDocApo";
    private static final String NUM_DOC_APO = "nDocApo";
    private static final String PAYMENT_DATE = "fechaPago";
    private static final String YEAR_PER = "anoPer";
    private static final String MONTH_PER = "mesPer";
    private static final String ID_TIPO_DOC_PERSONA = "idTipoDocPersona";
    private static final String ID_PERSONA = "idPersona";

    private static final String TYPE_DOC_AFI = "tDocAfi";

    private final SatConsultTransferableEmployerClientV2 satClientV2;
    private final WorkerRetirementNoveltyClient workerRetirementNoveltyClient;
    private final InsertLegalRepresentativeClient insertLegalRepresentativeClient;
    private final DependentRelationshipClient dependentRelationshipClient;
    private final ConsultSiarpAffiliateClient consultSiarpAffiliateClient;
    private final ConsultSiarpAffiliateRawClient consultSiarpAffiliateRawClient;
    private final ConsultSiarpAffiliateStatusClient consultSiarpAffiliateStatusClient;
    private final ConsultSiarpAffiliateStatusRawClient consultSiarpAffiliateStatusRawClient;
    private final InsertEmployerClient insertEmployerClient;
    private final UpdateEmployerClient updateEmployerClient;
    private final ConsultEmployerClient consultEmployerClient;
    private final ConsultEmployerRawClient consultEmployerRawClient;
    private final InsertPersonClient insertPersonClient;
    private final UpdatePersonClient updatePersonClient;
    private final InsertPolicyClient insertPolicyClient;
    private final ConsultPolicyClient consultPolicyClient;
    private final ConsultPolicyRawClient consultPolicyRawClient;
    private final ConsultBranchClient consultBranchClient;
    private final ConsultBranchRawClient consultBranchRawClient;
    private final ConsultHeadquartersClient consultHeadquartersClient;
    private final ConsultHeadquartersRawClient consultHeadquartersRawClient;
    private final InsertHeadquartersClient insertHeadquartersClient;
    private final InsertHeadquartersRawClient insertHeadquartersRawClient;
    private final UpdateHeadquartersClient updateHeadquartersClient;
    private final InsertWorkCenterClient insertWorkCenterClient;
    private final ConsultWorkCenterClient consultWorkCenterClient;
    private final ConsultWorkCenterRawClient consultWorkCenterRawClient;
    private final UpdateWorkCenterClient updateWorkCenterClient;
    private final UpdateWorkerPositionClient updateWorkerPositionClient;
    private final WorkerDisplacementNotificationClient workerDisplacementNotificationClient;
    private final UpdateIndependentEconomicActivityClient updateIndependentEconomicActivityClient;
    private final UpdateIndependentContractDateClient updateIndependentContractDateClient;
    private final UpdateIndependentOccupationClient updateIndependentOccupationClient;
    private final ConsultEmployerActivitiesClient consultEmployerActivitiesClient;
    private final ConsultEmployerActivitiesRawClient consultEmployerActivitiesRawClient;
    private final ConsultEmployerActivationClient consultEmployerActivationClient;
    private final ConsultEmployerActivationRawClient consultEmployerActivationRawClient;
    private final IndependentContractRelationshipClient independentContractRelationshipClient;
    private final WebhookEmployerClient webhookEmployerClient;
    private final RegistraduriaClient registraduriaClient;
    private final ConfecamarasClient confecamarasClient;
    private final VolunteerRelationshipClient volunteerRelationshipClient;
    private final ConsultUserPortalClient consultUserPortalClient;
    private final ConsultUserPortalRawClient consultUserPortalRawClient;
    private final PersonlClient personlClient;
    private final ConsultPersonRawClient consultPersonRawClient;
    private final ConsultLegalRepresentativeClient consultLegalRepresentativeClient;
    private final ConsultLegalRepresentativeRawClient consultLegalRepresentativeRawClient;
    private final ConsultBusinessGroupClient consultBusinessGroupClient;
    private final ConsultBusinessGroupRawClient consultBusinessGroupRawClient;
    private final ConsultAffiliateCompanyClient consultAffiliateCompanyClient;
    private final ConsultAffiliateCompanyRawClient consultAffiliateCompanyRawClient;
    private final ConsultContributorDataClient consultContributorDataClient;
    private final ConsultContributorDataRawClient consultContributorDataRawClient;
    private final ConsultMemberDataClient consultMemberDataClient;
    private final ConsultMemberDataRawClient consultMemberDataRawClient;
    private final ConsultBankLogClient consultBankLogClient;
    private final ConsultBankLogRawClient consultBankLogRawClient;
    private final GenericWebClient genericWebClient;

    // Insert/Update raw clients for operations
    private final InsertPersonRawClient insertPersonRawClient;
    private final UpdatePersonRawClient updatePersonRawClient;
    private final InsertEmployerRawClient insertEmployerRawClient;
    private final UpdateEmployerRawClient updateEmployerRawClient;
    private final InsertLegalRepresentativeRawClient insertLegalRepresentativeRawClient;
    private final InsertWorkCenterRawClient insertWorkCenterRawClient;
    private final UpdateWorkCenterRawClient updateWorkCenterRawClient;
    private final UpdateHeadquartersRawClient updateHeadquartersRawClient;
    private final InsertPolicyRawClient insertPolicyRawClient;
    private final UpdateWorkerPositionRawClient updateWorkerPositionRawClient;
    private final WorkerDisplacementNotificationRawClient workerDisplacementNotificationRawClient;
    private final DependentRelationshipRawClient dependentRelationshipRawClient;
    private final IndependentContractRelationshipRawClient independentContractRelationshipRawClient;
    private final VolunteerRelationshipRawClient volunteerRelationshipRawClient;
    private final UpdateIndependentEconomicActivityRawClient updateIndependentEconomicActivityRawClient;
    private final UpdateIndependentContractDateRawClient updateIndependentContractDateRawClient;
    private final UpdateIndependentOccupationRawClient updateIndependentOccupationRawClient;
    private final WorkerRetirementNoveltyRawClient workerRetirementNoveltyRawClient;

    @PostMapping(value = "/sat/transferable-employer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test SAT transferable employer v2", description = "Test SAT v2 with automatic telemetry")
    public ResponseEntity<TransferableEmployerResponse> testSatTransferableEmployer(
            @RequestBody TransferableEmployerRequest request) {
        
        // Just call the service - telemetry happens automatically!
        return ResponseEntity.ok(satClientV2.consult(request));
    }

    @PostMapping(value = "/sat/transferable-employer/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test SAT transferable employer v2 (raw)", description = "Returns SAT raw JSON with automatic telemetry")
    public ResponseEntity<String> testSatTransferableEmployerRaw(
            @RequestBody TransferableEmployerRequest request) {

        String raw = satClientV2.consultRaw(request);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/positiva/novelty/worker-retirement", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Positiva Worker Retirement Novelty", description = "Calls Positiva worker retirement with telemetry-enabled WebClient")
    public ResponseEntity<WorkerRetirementNoveltyResponse> testWorkerRetirement(
            @RequestBody WorkerRetirementNoveltyRequest request) {
        WorkerRetirementNoveltyResponse response = workerRetirementNoveltyClient.send(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/positiva/novelty/worker-retirement/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Positiva Worker Retirement Novelty (Raw)", description = "Returns worker retirement novelty raw JSON with automatic telemetry")
    public ResponseEntity<String> testWorkerRetirementRaw(@RequestBody WorkerRetirementNoveltyRequest request) {
        String raw = workerRetirementNoveltyRawClient.sendRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/legal-representative/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Legal Representative", description = "Calls Positiva legal representative insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertLegalRepresentative(
            @RequestBody LegalRepresentativeRequest request) {
        Object response = insertLegalRepresentativeClient.insertLegalRepresentative(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/legal-representative/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Legal Representative (Raw)", description = "Returns legal representative insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertLegalRepresentativeRaw(@RequestBody LegalRepresentativeRequest request) {
        String raw = insertLegalRepresentativeRawClient.insertLegalRepresentativeRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/dependent-relationship/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Dependent Relationship", description = "Calls dependent relationship insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertDependentRelationship(
            @RequestBody DependentRelationshipRequest request) {
        Object response = dependentRelationshipClient.insert(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/dependent-relationship/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Dependent Relationship (Raw)", description = "Returns dependent relationship insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertDependentRelationshipRaw(@RequestBody DependentRelationshipRequest request) {
        String raw = dependentRelationshipRawClient.insertRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/siarp/consultaAfiliado2", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test SIARP consultaAfiliado2 (parsed)", description = "Calls SIARP consultaAfiliado2 and parses response")
    public ResponseEntity<Object> testSiarpConsultaAfiliado2(@RequestBody Map<String, String> payload) {
        String tDoc = payload.getOrDefault("tDoc", "cc");
        String idAfiliado = payload.get(ID_AFILIADO_PARAM);
        var list = consultSiarpAffiliateClient.consult(tDoc, idAfiliado).blockOptional().orElse(java.util.List.of());
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/siarp/consultaAfiliado2/raw", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test SIARP consultaAfiliado2 (raw)", description = "Calls SIARP consultaAfiliado2 and returns raw JSON")
    public ResponseEntity<String> testSiarpConsultaAfiliado2Raw(@RequestBody Map<String, String> payload) {
        String tDoc = payload.getOrDefault("tDoc", "cc");
        String idAfiliado = payload.get(ID_AFILIADO_PARAM);
        String raw = consultSiarpAffiliateRawClient.consultRaw(tDoc, idAfiliado).blockOptional().orElse("[]");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
    }

    @PostMapping(value = "/siarp/consultaEstadoAfiliado", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test SIARP consultaEstadoAfiliado (parsed)", description = "Calls SIARP consultaEstadoAfiliado and parses response")
    public ResponseEntity<Object> testSiarpConsultaEstadoAfiliado(@RequestBody Map<String, String> payload) {
        String tDocEmp = payload.get("tDocEmp");
        String idEmp = payload.get("idEmp");
        String tDocAfi = payload.get(TYPE_DOC_AFI);
        String idAfi = payload.get("idAfi");
        var list = consultSiarpAffiliateStatusClient.consult(tDocEmp, idEmp, tDocAfi, idAfi).blockOptional().orElse(java.util.List.of());
        return ResponseEntity.ok(list);
    }

    @PostMapping(value = "/siarp/consultaEstadoAfiliado/raw", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test SIARP consultaEstadoAfiliado (raw)", description = "Calls SIARP consultaEstadoAfiliado and returns raw JSON")
    public ResponseEntity<String> testSiarpConsultaEstadoAfiliadoRaw(@RequestBody Map<String, String> payload) {
        String tDocEmp = payload.get("tDocEmp");
        String idEmp = payload.get("idEmp");
        String tDocAfi = payload.get(TYPE_DOC_AFI);
        String idAfi = payload.get("idAfi");
        String raw = consultSiarpAffiliateStatusRawClient.consultRaw(tDocEmp, idEmp, tDocAfi, idAfi).blockOptional().orElse("[]");
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(raw);
    }

    @PostMapping(value = "/employer/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Employer", description = "Calls employer insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertEmployer(@RequestBody EmployerRequest request) {
        Object response = insertEmployerClient.insertEmployer(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/employer/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Employer (Raw)", description = "Returns employer insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertEmployerRaw(@RequestBody EmployerRequest request) {
        String raw = insertEmployerRawClient.insertEmployerRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/employer/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Employer", description = "Calls employer update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateEmployer(@RequestBody EmployerRequest request) {
        Object response = updateEmployerClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/employer/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Employer (Raw)", description = "Returns employer update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateEmployerRaw(@RequestBody EmployerRequest request) {
        String raw = updateEmployerRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/person/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Person", description = "Calls person insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertPerson(@RequestBody PersonRequest request) {
        Object response = insertPersonClient.insertPerson(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/person/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Person (Raw)", description = "Returns person insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertPersonRaw(@RequestBody PersonRequest request) {
        String raw = insertPersonRawClient.insertPersonRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/person/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Person", description = "Calls person update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdatePerson(@RequestBody PersonRequest request) {
        Object response = updatePersonClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/person/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Person (Raw)", description = "Returns person update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdatePersonRaw(@RequestBody PersonRequest request) {
        String raw = updatePersonRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/policy/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Policy", description = "Calls policy insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertPolicy(@RequestBody InsertPolicyRequest request) {
        Object response = insertPolicyClient.insert(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/policy/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Policy (Raw)", description = "Returns policy insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertPolicyRaw(@RequestBody InsertPolicyRequest request) {
        String raw = insertPolicyRawClient.insertRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/workcenter/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Work Center", description = "Calls work center insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertWorkCenter(@RequestBody WorkCenterRequest request) {
        Object response = insertWorkCenterClient.insertWorkCenter(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/workcenter/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Work Center (Raw)", description = "Returns work center insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertWorkCenterRaw(@RequestBody WorkCenterRequest request) {
        String raw = insertWorkCenterRawClient.insertWorkCenterRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/workcenter/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Work Center (DTO)", description = "Calls work center consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultWorkCenter(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        List<Object> response = consultWorkCenterClient.consult(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/workcenter/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Work Center (Raw)", description = "Returns work center raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultWorkCenterRaw(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        String raw = consultWorkCenterRawClient.consultRaw(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/workcenter/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Work Center", description = "Calls work center update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateWorkCenter(@RequestBody UpdateWorkCenterRequest request) {
        Object response = updateWorkCenterClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/workcenter/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Work Center (Raw)", description = "Returns work center update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateWorkCenterRaw(@RequestBody UpdateWorkCenterRequest request) {
        String raw = updateWorkCenterRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/worker-position/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Worker Position/Occupation", description = "Calls worker position update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateWorkerPosition(@RequestBody UpdateWorkerPositionRequest request) {
        Object response = updateWorkerPositionClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/worker-position/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Worker Position (Raw)", description = "Returns worker position update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateWorkerPositionRaw(@RequestBody UpdateWorkerPositionRequest request) {
        String raw = updateWorkerPositionRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/worker-displacement/notify", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Worker Displacement Notification", description = "Calls worker displacement notification with telemetry-enabled WebClient")
    public ResponseEntity<Object> testWorkerDisplacementNotification(@RequestBody WorkerDisplacementNotificationRequest request) {
        Object response = workerDisplacementNotificationClient.notifyDisplacement(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/worker-displacement/notify/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Worker Displacement Notification (Raw)", description = "Returns worker displacement notification raw JSON with automatic telemetry")
    public ResponseEntity<String> testWorkerDisplacementNotificationRaw(@RequestBody WorkerDisplacementNotificationRequest request) {
        String raw = workerDisplacementNotificationRawClient.notifyDisplacementRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/independent-economic-activity/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Independent Economic Activity", description = "Calls independent economic activity update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateIndependentEconomicActivity(@RequestBody UpdateIndependentEconomicActivityRequest request) {
        Object response = updateIndependentEconomicActivityClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/independent-economic-activity/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Independent Economic Activity (Raw)", description = "Returns independent economic activity update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateIndependentEconomicActivityRaw(@RequestBody UpdateIndependentEconomicActivityRequest request) {
        String raw = updateIndependentEconomicActivityRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/independent-contract-date/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Independent Contract Date", description = "Calls independent contract date update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateIndependentContractDate(@RequestBody UpdateIndependentContractDateRequest request) {
        Object response = updateIndependentContractDateClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/independent-contract-date/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Independent Contract Date (Raw)", description = "Returns independent contract date update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateIndependentContractDateRaw(@RequestBody UpdateIndependentContractDateRequest request) {
        String raw = updateIndependentContractDateRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/independent-occupation/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Independent Occupation", description = "Calls independent occupation update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateIndependentOccupation(@RequestBody UpdateIndependentOccupationRequest request) {
        Object response = updateIndependentOccupationClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/independent-occupation/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Independent Occupation (Raw)", description = "Returns independent occupation update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateIndependentOccupationRaw(@RequestBody UpdateIndependentOccupationRequest request) {
        String raw = updateIndependentOccupationRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/independent-relationship/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Independent Relationship", description = "Calls independent contract relationship insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertIndependentRelationship(@RequestBody IndependentContractRelationshipRequest request) {
        Object response = independentContractRelationshipClient.insert(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/independent-relationship/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Independent Relationship (Raw)", description = "Returns independent relationship insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertIndependentRelationshipRaw(@RequestBody IndependentContractRelationshipRequest request) {
        String raw = independentContractRelationshipRawClient.insertRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/webhook/sync-employer", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Webhook Employer Sync", description = "Calls webhook employer sync with telemetry-enabled WebClient")
    public ResponseEntity<WebhookEmployerResponseDTO> testWebhookEmployerSync(@RequestBody WebhookEmployerRequestDTO request) {
        WebhookEmployerResponseDTO response = webhookEmployerClient.syncEmployer(request).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/employer/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Employer", description = "Calls employer consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<EmployerResponse>> testConsultEmployer(@RequestBody Map<String, Object> payload) {
        String tipoDoc = (String) payload.get(TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        List<EmployerResponse> response = consultEmployerClient.consult(tipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/employer/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Employer (Raw)", description = "Returns employer raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultEmployerRaw(@RequestBody Map<String, Object> payload) {
        String tipoDoc = (String) payload.get(TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        String raw = consultEmployerRawClient.consultRaw(tipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/registraduria/consult-identity", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Registraduria Consult Identity Card", description = "Calls Registraduria SOAP service with telemetry-enabled WebClient")
    public ResponseEntity<String> testRegistraduriaConsultIdentity(@RequestBody Map<String, String> payload) {
        String documentNumber = payload.get("documentNumber");
        String response = registraduriaClient.consultIdentityCard(documentNumber).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(response);
    }

    @PostMapping(value = "/confecamaras/consult-company", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Confecamaras Consult Company", description = "Calls Confecamaras API with telemetry-enabled WebClient")
    public ResponseEntity<RecordResponseDTO> testConfecamarasConsultCompany(@RequestBody Map<String, String> payload) {
        String nit = payload.get("nit");
        String dv = payload.get("dv");
        RecordResponseDTO response = confecamarasClient.consultCompany(nit, dv).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/volunteer-relationship/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Volunteer Relationship", description = "Calls volunteer relationship insert with telemetry-enabled WebClient")
    public ResponseEntity<Object> testInsertVolunteerRelationship(@RequestBody VolunteerRelationshipRequest request) {
        Object response = volunteerRelationshipClient.insert(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/volunteer-relationship/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Volunteer Relationship (Raw)", description = "Returns volunteer relationship insert raw JSON with automatic telemetry")
    public ResponseEntity<String> testInsertVolunteerRelationshipRaw(@RequestBody VolunteerRelationshipRequest request) {
        String raw = volunteerRelationshipRawClient.insertRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/user-portal/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test User Portal Consult", description = "Calls user portal consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<UserPortalResponse>> testConsultUserPortal(@RequestBody Map<String, String> payload) {
        String idTipoDocPersona = payload.get(ID_TIPO_DOC_PERSONA);
        String idPersona = payload.get(ID_PERSONA);
        List<UserPortalResponse> response = consultUserPortalClient.consult(idTipoDocPersona, idPersona).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/user-portal/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test User Portal Consult (Raw)", description = "Returns user portal raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultUserPortalRaw(@RequestBody Map<String, String> payload) {
        String idTipoDocPersona = payload.get(ID_TIPO_DOC_PERSONA);
        String idPersona = payload.get(ID_PERSONA);
        String raw = consultUserPortalRawClient.consultRaw(idTipoDocPersona, idPersona).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/person/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Person Consult", description = "Calls person consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<PersonResponse>> testConsultPerson(@RequestBody Map<String, String> payload) {
        String idTipoDocPersona = payload.get(ID_TIPO_DOC_PERSONA);
        String idPersona = payload.get(ID_PERSONA);
        List<PersonResponse> response = personlClient.consult(idTipoDocPersona, idPersona).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/person/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Person Consult (Raw)", description = "Returns person raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultPersonRaw(@RequestBody Map<String, String> payload) {
        String idTipoDocPersona = payload.get(ID_TIPO_DOC_PERSONA);
        String idPersona = payload.get(ID_PERSONA);
        String raw = consultPersonRawClient.consultRaw(idTipoDocPersona, idPersona).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/legal-representative/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Legal Representative Consult", description = "Calls legal representative consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<LegalRepresentativeResponse>> testConsultLegalRepresentative(@RequestBody Map<String, Object> payload) {
        String tipoDoc = (String) payload.get(TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        List<LegalRepresentativeResponse> response = consultLegalRepresentativeClient.consult(tipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/legal-representative/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Legal Representative Consult (Raw)", description = "Returns legal representative raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultLegalRepresentativeRaw(@RequestBody Map<String, Object> payload) {
        String tipoDoc = (String) payload.get(TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        String raw = consultLegalRepresentativeRawClient.consultRaw(tipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/business-group/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Business Group Consult", description = "Calls business group consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<BusinessGroupResponse>> testConsultBusinessGroup() {
        List<BusinessGroupResponse> response = consultBusinessGroupClient.getBusinessGroups().block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/business-group/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Business Group Consult (Raw)", description = "Returns business group raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultBusinessGroupRaw() {
        String raw = consultBusinessGroupRawClient.consultRaw().block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/affiliate-company/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Affiliate Company Consult", description = "Calls affiliate company consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<AffiliateCompanyResponse>> testConsultAffiliateCompany(@RequestBody Map<String, String> payload) {
        String tipoDoc = payload.get(TIPO_DOC_PARAM);
        String idAfiliado = payload.get(ID_AFILIADO_PARAM);
        List<AffiliateCompanyResponse> response = consultAffiliateCompanyClient.consultAffiliate(tipoDoc, idAfiliado).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/affiliate-company/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Affiliate Company Consult (Raw)", description = "Returns affiliate company raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultAffiliateCompanyRaw(@RequestBody Map<String, String> payload) {
        String tipoDoc = payload.get(TIPO_DOC_PARAM);
        String idAfiliado = payload.get(ID_AFILIADO_PARAM);
        String raw = consultAffiliateCompanyRawClient.consultRaw(tipoDoc, idAfiliado).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/generic/user-by-identification", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Get User by Identification", description = "Calls user identification service with telemetry-enabled WebClient")
    public ResponseEntity<UserDtoApiRegistry> testGetUserByIdentification(@RequestBody Map<String, String> payload) {
        String identification = payload.get("identification");
        var response = genericWebClient.getByIdentification(identification).orElse(null);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/generic/salary-by-year", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Get Salary by Year", description = "Calls salary service by year with telemetry-enabled WebClient")
    public ResponseEntity<SalaryDTO> testGetSalaryByYear(@RequestBody Map<String, Object> payload) {
        Integer year = (Integer) payload.get("year");
        SalaryDTO response = genericWebClient.getSmlmvByYear(year);
        return ResponseEntity.ok(response);
    }



    @PostMapping(value = "/generic/check-arrears-status", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Check User Arrears Status", description = "Calls user arrears check with telemetry-enabled WebClient")
    public ResponseEntity<Boolean> testCheckUserArrearsStatus(@RequestBody Map<String, String> payload) {
        String identificationNumber = payload.get("identificationNumber");
        String identificationType = payload.get("identificationType");
        Boolean response = genericWebClient.checkUserArrearsStatus(identificationNumber, identificationType);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/generic/all-occupations", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Get All Occupations", description = "Calls occupations service with telemetry-enabled WebClient")
    public ResponseEntity<List<Occupation>> testGetAllOccupations() {
        List<Occupation> response = genericWebClient.getAllOccupations();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/report/affiliate-card", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    @Operation(summary = "Test generate Affiliate Card report", description = "Calls /transversal/report/card via GenericWebClient with telemetry enabled")
    public ResponseEntity<String> testGenerateAffiliateCard(@RequestBody CertificateReportRequestDTO request) {
        String result = genericWebClient.generateAffiliateCard(request);
        return ResponseEntity.ok(result);
    }

    @PostMapping(value = "/policy/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Policy (DTO)", description = "Calls policy consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultPolicy(@RequestBody Map<String, String> payload) {
        String idTipoDoc = payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = payload.get(ID_EMPRESA_PARAM);
        List<Object> response = consultPolicyClient.consult(idTipoDoc, idEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/policy/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Policy (Raw)", description = "Returns policy raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultPolicyRaw(@RequestBody Map<String, String> payload) {
        String idTipoDoc = payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = payload.get(ID_EMPRESA_PARAM);
        String raw = consultPolicyRawClient.consultRaw(idTipoDoc, idEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/branch/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Branch/Sucursal (DTO)", description = "Calls branch consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultBranch(@RequestBody Map<String, String> payload) {
        String idTipoDoc = payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = payload.get(ID_EMPRESA_PARAM);
        List<Object> response = consultBranchClient.consult(idTipoDoc, idEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/branch/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Branch/Sucursal (Raw)", description = "Returns branch raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultBranchRaw(@RequestBody Map<String, String> payload) {
        String idTipoDoc = payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = payload.get(ID_EMPRESA_PARAM);
        String raw = consultBranchRawClient.consultRaw(idTipoDoc, idEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/headquarters/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Headquarters/Sedes (DTO)", description = "Calls headquarters consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultHeadquarters(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        List<Object> response = consultHeadquartersClient.consult(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/headquarters/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Headquarters/Sedes (Raw)", description = "Returns headquarters raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultHeadquartersRaw(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        String raw = consultHeadquartersRawClient.consultRaw(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/headquarters/update", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Headquarters/Sede", description = "Calls headquarters update with telemetry-enabled WebClient")
    public ResponseEntity<Object> testUpdateHeadquarters(@RequestBody UpdateHeadquartersRequest request) {
        Object response = updateHeadquartersClient.update(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/headquarters/update/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Update Headquarters (Raw)", description = "Returns headquarters update raw JSON with automatic telemetry")
    public ResponseEntity<String> testUpdateHeadquartersRaw(@RequestBody UpdateHeadquartersRequest request) {
        String raw = updateHeadquartersRawClient.updateRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/headquarters/insert", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Headquarters", description = "Insert a new sede/headquarters to Positiva")
    public ResponseEntity<Object> testInsertHeadquarters(@RequestBody InsertHeadquartersRequest request) {
        log.info("Testing insert headquarters with request: {}", request);
        return ResponseEntity.ok(insertHeadquartersClient.insert(request));
    }

    @PostMapping(value = "/headquarters/insert/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Insert Headquarters (Raw)", description = "Insert headquarters - returns raw JSON")
    public ResponseEntity<String> testInsertHeadquartersRaw(@RequestBody InsertHeadquartersRequest request) {
        log.info("Testing insert headquarters (raw) with request: {}", request);
        String raw = insertHeadquartersRawClient.insertRaw(request).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/employer-activities/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Employer Activities (DTO)", description = "Calls employer activities consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultEmployerActivities(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        List<Object> response = consultEmployerActivitiesClient.consult(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/employer-activities/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Employer Activities (Raw)", description = "Returns employer activities raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultEmployerActivitiesRaw(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        String raw = consultEmployerActivitiesRawClient.consultRaw(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/employer-activation/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Employer Activation (DTO)", description = "Calls employer activation consultation with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultEmployerActivation(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        List<Object> response = consultEmployerActivationClient.consult(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/employer-activation/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Employer Activation (Raw)", description = "Returns employer activation raw JSON with automatic telemetry")
    public ResponseEntity<String> testConsultEmployerActivationRaw(@RequestBody Map<String, Object> payload) {
        String idTipoDoc = (String) payload.get(ID_TIPO_DOC_PARAM);
        String idEmpresa = (String) payload.get(ID_EMPRESA_PARAM);
        Integer idSubEmpresa = (Integer) payload.get(ID_SUB_EMPRESA_PARAM);
        String raw = consultEmployerActivationRawClient.consultRaw(idTipoDoc, idEmpresa, idSubEmpresa).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/recaudos/contributor-data/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Contributor Data (DTO)", description = "Calls contributor/payer data consultation from Recaudos with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultContributorData(@RequestBody Map<String, String> payload) {
        String tDocApo = payload.get(TYPE_DOC_APO);
        String nDocApo = payload.get(NUM_DOC_APO);
        String fechaPago = payload.get(PAYMENT_DATE);
        String anoPer = payload.get(YEAR_PER);
        String mesPer = payload.get(MONTH_PER);
        List<Object> response = consultContributorDataClient.consult(tDocApo, nDocApo, fechaPago, anoPer, mesPer).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/recaudos/contributor-data/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Contributor Data (Raw)", description = "Returns contributor/payer data raw JSON from Recaudos with automatic telemetry")
    public ResponseEntity<String> testConsultContributorDataRaw(@RequestBody Map<String, String> payload) {
        String tDocApo = payload.get(TYPE_DOC_APO);
        String nDocApo = payload.get(NUM_DOC_APO);
        String fechaPago = payload.get(PAYMENT_DATE);
        String anoPer = payload.get(YEAR_PER);
        String mesPer = payload.get(MONTH_PER);
        String raw = consultContributorDataRawClient.consultRaw(tDocApo, nDocApo, fechaPago, anoPer, mesPer).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/recaudos/member-data/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Member Data (DTO)", description = "Calls member/contributor payment data consultation from Recaudos with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultMemberData(@RequestBody Map<String, String> payload) {
        String tDocApo = payload.get(TYPE_DOC_APO);
        String nDocApo = payload.get(NUM_DOC_APO);
        String tDocAfi = payload.get(TYPE_DOC_AFI);
        String nDocAfi = payload.get("nDocAfi");
        String fechaPago = payload.get(PAYMENT_DATE);
        String anoPer = payload.get(YEAR_PER);
        String mesPer = payload.get(MONTH_PER);
        List<Object> response = consultMemberDataClient.consult(tDocApo, nDocApo, tDocAfi, nDocAfi, fechaPago, anoPer, mesPer).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/recaudos/member-data/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Member Data (Raw)", description = "Returns member/contributor payment data raw JSON from Recaudos with automatic telemetry")
    public ResponseEntity<String> testConsultMemberDataRaw(@RequestBody Map<String, String> payload) {
        String tDocApo = payload.get(TYPE_DOC_APO);
        String nDocApo = payload.get(NUM_DOC_APO);
        String tDocAfi = payload.get(TYPE_DOC_AFI);
        String nDocAfi = payload.get("nDocAfi");
        String fechaPago = payload.get(PAYMENT_DATE);
        String anoPer = payload.get(YEAR_PER);
        String mesPer = payload.get(MONTH_PER);
        String raw = consultMemberDataRawClient.consultRaw(tDocApo, nDocApo, tDocAfi, nDocAfi, fechaPago, anoPer, mesPer).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }

    @PostMapping(value = "/recaudos/bank-log/consult", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Bank Log (DTO)", description = "Calls bank payment log consultation from Recaudos with telemetry-enabled WebClient")
    public ResponseEntity<List<Object>> testConsultBankLog(@RequestBody Map<String, String> payload) {
        String nDocApo = payload.get(NUM_DOC_APO);
        String anoPer = payload.get(YEAR_PER);
        String mesPer = payload.get(MONTH_PER);
        String fechaPago = payload.get(PAYMENT_DATE);
        List<Object> response = consultBankLogClient.consult(nDocApo, anoPer, mesPer, fechaPago).block();
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/recaudos/bank-log/consult/raw", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Test Consult Bank Log (Raw)", description = "Returns bank payment log raw JSON from Recaudos with automatic telemetry")
    public ResponseEntity<String> testConsultBankLogRaw(@RequestBody Map<String, String> payload) {
        String nDocApo = payload.get(NUM_DOC_APO);
        String anoPer = payload.get(YEAR_PER);
        String mesPer = payload.get(MONTH_PER);
        String fechaPago = payload.get(PAYMENT_DATE);
        String raw = consultBankLogRawClient.consultRaw(nDocApo, anoPer, mesPer, fechaPago).block();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(raw);
    }
}
