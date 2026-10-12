package be.pxl.services.notification.service;

import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    public String senNotification(String message){
        System.out.println("Notification: " + message);
        return "Notification processed... " + message;
    }
}
