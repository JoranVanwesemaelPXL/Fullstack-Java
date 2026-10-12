package be.pxl.services.notification.controller;

import be.pxl.services.notification.service.NotificationService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService service){
        this.notificationService = service;
    }

    @PostMapping
    public String sendNotification(@RequestBody String message){
        return notificationService.senNotification(message);
    }
}
