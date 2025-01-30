package com.example;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MessageController{

    @GetMapping("/message_service")
    public String messageService(){
        return "No functional from Message Service yet!";
    }

}
