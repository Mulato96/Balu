package com.gal.afiliaciones.infrastructure.controller.employeeupdateinfo;


import com.gal.afiliaciones.application.service.employeeupdateinfo.IUpdateInfoEmployeeInfoService;
import com.gal.afiliaciones.config.BodyResponseConfig;
import com.gal.afiliaciones.infrastructure.dto.employeeupdateinfo.UpdateInfoEmployeeIndependentRequest;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/updateIndependentInfoEmployee")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class UpdateInfoEmployeeIndependentController {


    private final IUpdateInfoEmployeeInfoService service;


    @PutMapping
    public ResponseEntity<BodyResponseConfig<String>> updateInfoEmployeeIndependent (
            @RequestBody @Valid UpdateInfoEmployeeIndependentRequest request)  throws MessagingException, IOException {

        return new ResponseEntity<>(new BodyResponseConfig<>(
                service.updateInfoEmployeeIndependet(request)), HttpStatus.OK);
    }



}
