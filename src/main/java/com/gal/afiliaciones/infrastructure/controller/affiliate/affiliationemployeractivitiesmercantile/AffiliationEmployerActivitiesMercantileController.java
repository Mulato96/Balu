package com.gal.afiliaciones.infrastructure.controller.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.application.service.affiliate.FiledWebSocketService;
import com.gal.afiliaciones.application.service.affiliate.ScheduleInterviewWebService;
import com.gal.afiliaciones.application.service.affiliate.affiliationemployeractivitiesmercantile.AffiliationEmployerActivitiesMercantileService;
import com.gal.afiliaciones.application.service.notification.RegistryConnectInterviewWebService;
import com.gal.afiliaciones.application.service.observationsaffiliation.ObservationsAffiliationService;
import com.gal.afiliaciones.domain.model.DateInterviewWeb;
import com.gal.afiliaciones.domain.model.ObservationsAffiliation;
import com.gal.afiliaciones.domain.model.affiliate.affiliationworkedemployeractivitiesmercantile.AffiliateMercantile;
import com.gal.afiliaciones.infrastructure.dto.affiliate.DateInterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.AffiliateMercantileDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataBasicCompanyDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.DataLegalRepresentativeDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliate.affiliationemployeractivitiesmercantile.InterviewWebDTO;
import com.gal.afiliaciones.infrastructure.dto.affiliationemployerdomesticserviceindependent.DocumentsDTO;
import com.gal.afiliaciones.infrastructure.dto.typeemployerdocument.DocumentRequestDTO;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/affiliationemployeractivitiesmercantile")
@AllArgsConstructor
public class AffiliationEmployerActivitiesMercantileController {

    private final FiledWebSocketService filedWebSocketService;
    private final ScheduleInterviewWebService scheduleInterviewWebService;
    private final ObservationsAffiliationService observationsAffiliationService;
    private final RegistryConnectInterviewWebService registryConnectInterviewWebService;
    private final AffiliationEmployerActivitiesMercantileService affiliationEmployerActivitiesMercantileService;


    @GetMapping("/validationsStepOne/{numberDocument}/{typeDocument}/{dv}")
    public ResponseEntity<DataBasicCompanyDTO> validationsStepOne(@PathVariable String numberDocument, @PathVariable String typeDocument, @PathVariable String dv){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.validationsStepOne(numberDocument, typeDocument, dv));
    }

    @PostMapping("/stepone")
    public ResponseEntity<AffiliateMercantile> stepOne(@RequestBody DataBasicCompanyDTO dataBasicCompanyDTO){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.stepOne(dataBasicCompanyDTO));
    }

    @PostMapping("update-data-regularization-step-one")
    public ResponseEntity<AffiliateMercantile> updateDataRegularization(@RequestBody DataBasicCompanyDTO dataBasicCompanyDTO){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.updateDataRegularizationStepOne(dataBasicCompanyDTO));
    }

    @PostMapping("/findUser")
    public ResponseEntity<DataLegalRepresentativeDTO> findUser(@RequestBody AffiliateMercantile affiliateMercantile){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.findUser(affiliateMercantile));
    }

    @PostMapping("/steptwo")
    public ResponseEntity<AffiliateMercantile> stepTwo(@RequestBody DataLegalRepresentativeDTO dataLegalRepresentativeDTO){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.stepTwo(dataLegalRepresentativeDTO, false));
    }

    @PostMapping("/update-data-regularization-step-two")
    public ResponseEntity<AffiliateMercantile> updateDataRegularizationStepTwo(@RequestBody DataLegalRepresentativeDTO dataLegalRepresentativeDTO){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.updateDataRegularizationStepTwo(dataLegalRepresentativeDTO));
    }


    @PutMapping(value = "/stepthree/{idAffiliation}/{idTypeEmployer}/{idSubTypeEmployer}")
    public ResponseEntity<AffiliateMercantileDTO> stepThree(@PathVariable Long idAffiliation , @PathVariable Long idTypeEmployer , @PathVariable Long idSubTypeEmployer ,  @RequestBody List<DocumentRequestDTO> files){
        return ResponseEntity.ok().body(affiliationEmployerActivitiesMercantileService.stepThree(idAffiliation, idTypeEmployer, idSubTypeEmployer,  files));
    }

    @PutMapping("/statedocuments/{idAffiliate}")
    public void stateDocuments(@RequestBody List<DocumentsDTO> listDocumentsDTOS, @PathVariable Long idAffiliate){
        affiliationEmployerActivitiesMercantileService.stateDocuments(listDocumentsDTOS, idAffiliate);
    }


    @PostMapping("/scheduleinterviewweb")
    public ResponseEntity<Map<String, Object>> scheduleInterviewWeb(@RequestBody DateInterviewWebDTO dateInterviewWebDTO){

        Map<String, Object> map = affiliationEmployerActivitiesMercantileService.scheduleInterviewWeb(dateInterviewWebDTO);
        return ResponseEntity.ok().body(map);
    }

    @DeleteMapping("/deleteInterviewWeb/{idAffiliate}")
    public ResponseEntity<String> deleteInterviewWeb(@PathVariable String idAffiliate){
        affiliationEmployerActivitiesMercantileService.changeAffiliation(idAffiliate);
        filedWebSocketService.reschedulingInterviewWeb(idAffiliate, null);
        return ResponseEntity.ok().body(scheduleInterviewWebService.deleteInterviewWeb(idAffiliate));
    }

    @GetMapping("/viewinterview")
    public ResponseEntity<List<DateInterviewWeb>> viewInterview(){
        return ResponseEntity.ok().body(scheduleInterviewWebService.listScheduleInterviewWeb());
    }

    @GetMapping("/meshTimetable/{date}")
    public ResponseEntity<List<Map<String, LocalDateTime>>> meshTimetable(@PathVariable LocalDate date){
        return ResponseEntity.ok().body(scheduleInterviewWebService.meshTimetable(date));
    }

    @GetMapping("/daySkilled/{date}")
    public ResponseEntity<LocalDate> daysSkilled(@PathVariable LocalDate date){
        return ResponseEntity.ok().body(scheduleInterviewWebService.calculateDaysSkilled(date));
    }


    @PutMapping("/updateDataInterviewWeb")
    public ResponseEntity<String> updateDataInterviewWeb(@RequestBody InterviewWebDTO interviewWebDTO){

        return ResponseEntity.ok().body( affiliationEmployerActivitiesMercantileService.updateDataInterviewWeb(interviewWebDTO));
    }


    @PostMapping("/rescheduling")
    public ResponseEntity<String> rescheduling(@RequestBody ObservationsAffiliation observationAffiliation){

        affiliationEmployerActivitiesMercantileService.changeAffiliation(observationAffiliation.getFiledNumber());

        if(observationAffiliation.getObservations() != null && !(observationAffiliation.getObservations().isEmpty())){

            observationsAffiliationService.create(observationAffiliation.getObservations(), observationAffiliation.getFiledNumber(), observationAffiliation.getReasonReject(), observationAffiliation.getIdOfficial());
            filedWebSocketService.reschedulingInterviewWeb(observationAffiliation.getFiledNumber(), LocalDateTime.now());
        }

        filedWebSocketService.reschedulingInterviewWeb(observationAffiliation.getFiledNumber(), null);
        String message = scheduleInterviewWebService.deleteInterviewWebReSchedule(observationAffiliation.getFiledNumber());

        registryConnectInterviewWebService.deleteByFiledNumber(observationAffiliation.getFiledNumber());

        return ResponseEntity.ok().body(message);

    }

}
