package com.gal.afiliaciones.infrastructure.controller.notifications;

import com.gal.afiliaciones.application.service.affiliate.FiledWebSocketService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@AllArgsConstructor
@RequestMapping("/notifications")
public class NotificationsController {

    private final FiledWebSocketService filedWebSocketService;



    @PostMapping("/userConnect")
    public ResponseEntity<String> userConnect(@RequestBody Map<String, String> numberFiled){
        filedWebSocketService.connectUser(numberFiled, "connected");
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/officialConnect")
    public ResponseEntity<String> officialConnect(@RequestBody Map<String, String> numberFiled){
        filedWebSocketService.connectOfficial(numberFiled, "connected");
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/userDisconnect")
    public ResponseEntity<String> userDisconnect(@RequestBody Map<String, String> numberFiled){
        filedWebSocketService.connectUser(numberFiled, "disconnected");
        return ResponseEntity.ok().body("OK");
    }

    @PostMapping("/officialDisconnect")
    public ResponseEntity<String> officialDisconnect(@RequestBody Map<String, String> numberFiled){
        filedWebSocketService.connectOfficial(numberFiled, "disconnected");
        return ResponseEntity.ok().body("OK");
    }

    @GetMapping("/notificationByFiledNumber/{filedNumber}")
    public ResponseEntity<Map<String, String>> notificationByFiledNumber(@PathVariable String filedNumber){
        return ResponseEntity.ok().body(filedWebSocketService.notificationByFiledNumber(filedNumber));
    }


}
