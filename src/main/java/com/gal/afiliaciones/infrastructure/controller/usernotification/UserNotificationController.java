package com.gal.afiliaciones.infrastructure.controller.usernotification;

import com.gal.afiliaciones.application.service.usernotification.UserNotificationService;
import com.gal.afiliaciones.infrastructure.dto.usernotification.UserNotificationDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/usernotification")
@RequiredArgsConstructor
@Tag(name = "USER_NOTIFICATION", description = "Servicios para envio de notificaciones")
@CrossOrigin("*")
public class UserNotificationController {

    private final UserNotificationService service;

    @GetMapping("findAllAffiliatedUser")
    public ResponseEntity<List<UserNotificationDTO>> findAllAffiliatedUser() {
        List<UserNotificationDTO> userList = service.findAllAffiliatedUser();
        return ResponseEntity.ok(userList);
    }

}
