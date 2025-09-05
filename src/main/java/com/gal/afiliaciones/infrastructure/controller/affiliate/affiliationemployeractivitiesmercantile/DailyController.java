package com.gal.afiliaciones.infrastructure.controller.affiliate.affiliationemployeractivitiesmercantile;

import com.gal.afiliaciones.application.service.daily.DailyService;
import com.gal.afiliaciones.infrastructure.dto.daily.DailyRoomsDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.DataDailyDTO;
import com.gal.afiliaciones.infrastructure.dto.daily.TokenDailyDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/daily")
@Tag(name="API daily", description = "Framework para las entrevistas")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class DailyController {

    private final DailyService dailyService;

    @PostMapping("/tokenUser")
    public ResponseEntity<String> tokenUser(@RequestBody TokenDailyDTO tokenDailyDTO){
        return ResponseEntity.ok().body(dailyService.createTokenUser(tokenDailyDTO));
    }

    @GetMapping("/createRoom/")
    public ResponseEntity<DailyRoomsDTO> createRoom(@PathVariable Long idOfficial){
        return  ResponseEntity.ok().body(dailyService.createRoom());
    }

    @GetMapping("/tokenOfficial/{idOfficial}")
    public ResponseEntity<String> tokenOfficial(@PathVariable Long idOfficial){
        return  ResponseEntity.ok().body(dailyService.createTokenOfficial(idOfficial));
    }

    @GetMapping("/startMeet/{idOfficial}")
    public ResponseEntity<DataDailyDTO> startMeet(@PathVariable Long idOfficial){
        return ResponseEntity.ok().body(dailyService.startMeet(idOfficial));
    }

}
