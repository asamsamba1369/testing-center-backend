package edu.stonybrook.testingcenter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HelloController {

    // A simple GET endpoint to test
    @GetMapping("/hello")
    public String sayHello() {
        return "Hello from the Testing Center Reservation System!";
    }
}
